package Purple.Purple.user.controller;

import Purple.Purple.preferences.entity.PreferencesEntity;
import Purple.Purple.preferences.repository.PreferencesRepository;
import Purple.Purple.user.dto.*;
import Purple.Purple.user.entity.UserPersonalInfo;
import Purple.Purple.user.jwt.JwtUtil;
import Purple.Purple.user.repository.RefreshRepository;
import Purple.Purple.user.repository.UserRepository;
import Purple.Purple.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "유저 인증 및 관리 API")
public class UserController {
    private final UserService userService;
    private final UserRepository userRepository;
    @Operation(summary = "회원가입", description = "회원가입을 처리합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "회원가입 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "409", description = "이미 존재하는 이메일/닉네임")
    })
    @PostMapping("/signup")
    public ResponseEntity<UserResponse> signup(@RequestBody SignupRequest request) {
        UserResponse response = userService.signup(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)                     // 201 Created
                .body(response);
    }

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RefreshRepository refreshRepository;
    
    @Autowired
    private PreferencesRepository preferencesRepository;

    @Operation(summary = "로그인", description = "로그인을 처리합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 인증 정보"),
            @ApiResponse(responseCode = "404", description = "사용자 없음")
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest req, HttpServletResponse response) {
        LoginResponse loginResponse = userService.login(req);

        // UserService에서 생성한 refreshToken을 가져오기 위해 user 조회
        UserPersonalInfo user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        // 사용자 선호도 정보 조회 (activityPreference 포함)
        Integer activityPreference = null;
        PreferencesEntity preferences = preferencesRepository.findByUser_UserId(user.getUserId()).orElse(null);
        if (preferences != null) {
            activityPreference = preferences.getActivityPreference();
        }
        
        String refreshToken = jwtUtil.createJwt(
                "refresh",
                user.getEmail(),
                user.getRole(),
                user.getUserId(),
                activityPreference,
                7 * 24 * 60 * 60 * 1000L
        );

        Cookie refreshCookie = new Cookie("refreshToken", refreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(7 * 24 * 60 * 60);
        response.addCookie(refreshCookie);


        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + loginResponse.getToken());
        //headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
        //Map<String,String> body = Map.of("token", accessToken);
        return new ResponseEntity<>(loginResponse, headers, HttpStatus.OK);
    }


    @Operation(summary = "로그아웃", description = "로그아웃을 처리합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "로그아웃 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "사용자 없음")
    })
    @PostMapping("/{userId}/logout")
    public ResponseEntity<String> logout(
            @Parameter(description = "유저 ID", example = "1") @PathVariable Long userId) {
        return ResponseEntity.ok("User " + userId + " 로그아웃 완료");
    }

    @Operation(summary = "회원탈퇴", description = "회원 탈퇴를 처리합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "회원탈퇴 성공"),
            @ApiResponse(responseCode = "404", description = "사용자 없음")
    })
    @DeleteMapping("/{userId}/withdraw")
    public ResponseEntity<String> withdraw(@PathVariable Long userId) {
        {
            userService.withdraw(userId);
            return ResponseEntity.noContent().build();
        }
    }

    @Operation(summary = "사용자 정보 조회", description = "사용자 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "사용자 없음")
    })
    @GetMapping("/{userId}/me")
    public ResponseEntity<UserResponse> getUserInfo(@Parameter(description = "UserID",example = "1")@PathVariable Long userId) {
        UserResponse response = userService.getUserInfo(userId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "사용자 정보 수정", description = "사용자 정보를 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "404", description = "사용자 없음")
    })
    @PutMapping("/{userId}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long userId,
            @RequestBody UserUpdateRequest request
    )  {
        UserResponse response = userService.updateUser(userId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "비밀번호 변경", description = "현재 비밀번호 확인 후 새 비밀번호로 변경합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "비밀번호 변경 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "404", description = "사용자 없음")
    })
    @PutMapping("/{userId}/password")
    public ResponseEntity<String> changePassword(
            @PathVariable Long userId,
            @RequestBody PasswordChangeRequest request
    ) {
        userService.changePassword(userId, request);
        return ResponseEntity.ok("비밀번호가 성공적으로 변경되었습니다.");
    }




    // HTTP 헤더에서 토큰을 추출하는 헬퍼 메서드
    private String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        return (bearer != null && bearer.startsWith("Bearer ")) ? bearer.substring(7) : null;
    }

}
