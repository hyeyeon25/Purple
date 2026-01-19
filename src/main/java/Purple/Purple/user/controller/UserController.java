package Purple.Purple.user.controller;

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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 유저 인증 및 관리 컨트롤러
 * - 회원가입, 로그인, 로그아웃 관리 기능 제공함
 */

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "유저 인증 및 관리 API")
public class UserController {
    private final UserService userService;
    private final UserRepository userRepository;

    // 회원가입: 신규 유저 정보 저장 및 201(Created) 응답 반환
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
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RefreshRepository refreshRepository;

    @Operation(summary = "로그인", description = "로그인을 처리합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 인증 정보"),
            @ApiResponse(responseCode = "404", description = "사용자 없음")
    })

    // 로그인: 사용자 인증 후 Access Token(헤더) 및 Refresh Token(쿠키) 생성하여 전달
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest req, HttpServletResponse response) {
        LoginResponse loginResponse = userService.login(req);

        // 유저 정보 조회 후 7일 유효기간의 Refresh Token 생성함
        UserPersonalInfo user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        String refreshToken = jwtUtil.createJwt(
                "refresh",
                user.getEmail(),
                user.getRole(),
                user.getUserId(),
                7 * 24 * 60 * 60 * 1000L
        );

        Cookie refreshCookie = new Cookie("refreshToken", refreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(7 * 24 * 60 * 60);
        response.addCookie(refreshCookie);


        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + loginResponse.getToken());
        return new ResponseEntity<>(loginResponse, headers, HttpStatus.OK);
    }

    // 로그아웃: 본인 확인 후 성공 응답 반환함 (실제 토큰 무효화는 시큐리티 필터에서 수행)
    @Operation(summary = "로그아웃", description = "로그아웃을 처리합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "로그아웃 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "권한 없음 (본인 계정만 로그아웃 가능)"),
            @ApiResponse(responseCode = "404", description = "사용자 없음")
    })
    @PostMapping("/{userId}/logout")
    public ResponseEntity<Void> logout(
            @Parameter(description = "유저 ID", example = "1") @PathVariable Long userId,
            @AuthenticationPrincipal UserPersonalInfo userPersonalInfo) {
        
        if (userPersonalInfo == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        // 본인 계정만 로그아웃 가능
        if (!userPersonalInfo.getUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        // 로그아웃은 CustomLogoutFilter에서 처리되므로 여기서는 성공 응답만 반환
        // 실제 refresh 토큰 삭제는 CustomLogoutFilter에서 처리됨
        return ResponseEntity.noContent().build();
    }

    // 회원탈퇴: 본인 인증 후 서비스 레이어 호출하여 유저 데이터 삭제
    @Operation(summary = "회원탈퇴", description = "회원 탈퇴를 처리합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "회원탈퇴 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "권한 없음 (본인 계정만 삭제 가능)"),
            @ApiResponse(responseCode = "404", description = "사용자 없음")
    })
    @DeleteMapping("/{userId}/withdraw")
    public ResponseEntity<Void> withdraw(
            @PathVariable Long userId,
            @AuthenticationPrincipal UserPersonalInfo userPersonalInfo) {
        
        if (userPersonalInfo == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        // 본인 계정만 삭제 가능
        if (!userPersonalInfo.getUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        try {
            userService.withdraw(userId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            // 외래키 제약조건 등 기타 예외
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // 정보조회: 로그인한 본인의 상세 유저 정보 조회함
    @Operation(summary = "사용자 정보 조회", description = "사용자 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "권한 없음 (본인 정보만 조회 가능)"),
            @ApiResponse(responseCode = "404", description = "사용자 없음")
    })
    @GetMapping("/{userId}/me")
    public ResponseEntity<UserResponse> getUserInfo(
            @Parameter(description = "UserID",example = "1")@PathVariable Long userId,
            @AuthenticationPrincipal UserPersonalInfo userPersonalInfo) {
        
        if (userPersonalInfo == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        // 본인 정보만 조회 가능
        if (!userPersonalInfo.getUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        try {
            UserResponse response = userService.getUserInfo(userId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // 정보수정: 닉네임 등 유저 프로필 업데이트 수행함
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
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (현재 비밀번호 불일치)"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "권한 없음 (본인 계정만 변경 가능)"),
            @ApiResponse(responseCode = "404", description = "사용자 없음")
    })
    @PutMapping("/{userId}/password")
    public ResponseEntity<String> changePassword(
            @PathVariable Long userId,
            @RequestBody PasswordChangeRequest request,
            @AuthenticationPrincipal UserPersonalInfo userPersonalInfo
    ) {
        if (userPersonalInfo == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        // 본인 계정만 비밀번호 변경 가능
        if (!userPersonalInfo.getUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        try {
            userService.changePassword(userId, request);
            return ResponseEntity.ok("비밀번호가 성공적으로 변경되었습니다.");
        } catch (IllegalArgumentException e) {
            // 현재 비밀번호 불일치 또는 사용자 없음
            if (e.getMessage().contains("비밀번호")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }




    // 토큰 파싱: Authorization 헤더에서 Bearer 제외한 토큰 문자열만 추출함
    private String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        return (bearer != null && bearer.startsWith("Bearer ")) ? bearer.substring(7) : null;
    }

}
