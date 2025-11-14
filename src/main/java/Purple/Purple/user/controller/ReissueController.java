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

@RestController
@RequiredArgsConstructor
public class ReissueController {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private RefreshRepository refreshRepository;

    @PostMapping("/reissue")
    public ResponseEntity<?> reissue(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = extractRefreshTokenFromCookies(request);

        if (refreshToken == null || !jwtUtil.validate(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Refresh token invalid or missing");
        }


        String category = jwtUtil.getCategory(refreshToken);
        if (!"refresh".equals(category)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Invalid refresh token");
        }

        //DB에 저장되어 있는지 확인
        Boolean isExist = refreshRepository.existsByRefresh(refreshToken);
        if (!isExist) {

            //response body
            return new ResponseEntity<>("invalid refresh token", HttpStatus.BAD_REQUEST);
        }

        Long userId = jwtUtil.getUserIdFromToken(refreshToken);
        Optional<UserPersonalInfo> optionalUser = userRepository.findById(userId);

        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
        }

        UserPersonalInfo user = optionalUser.get();

        String newAccessToken = jwtUtil.createJwt(
                "access",
                user.getEmail(),
                user.getRole(),
                user.getUserId(),
                10 * 60 * 1000L  // 10분
        );

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
        response.addCookie(refreshCookie);// 나중에 함수로 빼둘 수 있으면 빼두기

        refreshRepository.deleteByRefresh(newRefreshToken);
        addRefreshEntity(user.getEmail(), newRefreshToken,7 * 24 * 60 * 60 * 1000L);

        return ResponseEntity.ok("Access token reissued");
    }

    private void addRefreshEntity(String username, String refresh, Long expiredMs) {

        Date date = new Date(System.currentTimeMillis() + expiredMs);

        RefreshEntity refreshEntity = new RefreshEntity();
        refreshEntity.setUsername(username);
        refreshEntity.setRefresh(refresh);
        refreshEntity.setExpiration(date.toString());

        refreshRepository.save(refreshEntity);
    }



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
