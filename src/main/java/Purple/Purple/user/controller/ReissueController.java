package Purple.Purple.user.controller;

import Purple.Purple.user.entity.RefreshEntity;
import Purple.Purple.user.entity.UserPersonalInfo;
import Purple.Purple.user.jwt.JwtUtil;
import Purple.Purple.user.repository.RefreshRepository;
import Purple.Purple.user.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;
import java.util.Optional;


/**
 * Access Token 만료 시 Refresh Token을 이용하여 토큰을 재발급하는 컨트롤러
 */
@RestController
@RequiredArgsConstructor
public class ReissueController {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private RefreshRepository refreshRepository;

    /**
     * 클라이언트의 쿠키에서 Refresh Token을 추출하여 유효성을 검증한 뒤,
     * 새로운 Access/Refresh 토큰 세트를 발급
     */

    @PostMapping("/reissue")
    public ResponseEntity<?> reissue(HttpServletRequest request, HttpServletResponse response) {

        //요청 쿠키에서 Refresh Token 추출
        String refreshToken = extractRefreshTokenFromCookies(request);
        //토큰 존재 여부 및 유효성 검증
        if (refreshToken == null || !jwtUtil.validate(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Refresh token invalid or missing");
        }

        //토큰의 카테고리가 refresh인지 확인
        String category = jwtUtil.getCategory(refreshToken);
        if (!"refresh".equals(category)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Invalid refresh token");
        }

        //DB에 refresh가 저장되어 있는지 확인(Refresh Token Rotation 또는 서버측 무효화 검증)
        Boolean isExist = refreshRepository.existsByRefresh(refreshToken);
        if (!isExist) {
            return new ResponseEntity<>("invalid refresh token", HttpStatus.BAD_REQUEST);
        }

        //토큰에서 사용자 정보 추출 및 존재 여부 확인
        Long userId = jwtUtil.getUserIdFromToken(refreshToken);
        Optional<UserPersonalInfo> optionalUser = userRepository.findById(userId);

        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
        }

        UserPersonalInfo user = optionalUser.get();

        //새로운 Access Token 생성(10분)
        String newAccessToken = jwtUtil.createJwt(
                "access",
                user.getEmail(),
                user.getRole(),
                user.getUserId(),
                10 * 60 * 1000L
        );

        //새로운 Refresh Token 생성(7일)
        String newRefreshToken = jwtUtil.createJwt(
                "refresh",
                user.getEmail(),
                user.getRole(),
                user.getUserId(),
                7 * 24 * 60 * 60 * 1000L
        );


        response.setHeader("Authorization", "Bearer " + newAccessToken);
        Cookie refreshCookie = new Cookie("refresh", newRefreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(true);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(7 * 24 * 60 * 60);
        response.addCookie(refreshCookie);

        refreshRepository.deleteByRefresh(newRefreshToken);
        addRefreshEntity(user.getEmail(), newRefreshToken,7 * 24 * 60 * 60 * 1000L);

        return ResponseEntity.ok("Access token reissued");
    }

    /**
     * 발급된 Refresh Token 정보를 DB에 저장함
     */
    private void addRefreshEntity(String username, String refresh, Long expiredMs) {

        Date date = new Date(System.currentTimeMillis() + expiredMs);

        RefreshEntity refreshEntity = new RefreshEntity();
        refreshEntity.setUsername(username);
        refreshEntity.setRefresh(refresh);
        refreshEntity.setExpiration(date.toString());

        refreshRepository.save(refreshEntity);
    }


    /**
     * HttpServletRequest에서 "refreshToken" 이름을 가진 쿠키의 값을 추출
     */
    private String extractRefreshTokenFromCookies(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("refreshToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
