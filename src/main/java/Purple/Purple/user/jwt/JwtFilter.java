package Purple.Purple.user.jwt;

import Purple.Purple.user.entity.UserPersonalInfo;
import Purple.Purple.user.repository.UserRepository;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;


    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String requestUri = request.getRequestURI();
        log.info("JwtFilter - Request URI: {}", requestUri);

        if (isPublicPath(requestUri)) {
            log.info("JwtFilter - Public path, skipping authentication");
            filterChain.doFilter(request, response);
            return;
        }

        String accessToken = request.getHeader("Authorization");
        log.info("JwtFilter - Authorization header: {}", accessToken != null ? "Bearer ***" : "null");

        if (accessToken == null || !accessToken.startsWith("Bearer ")) {
            log.warn("JwtFilter - No valid Authorization header, returning 401. URI: {}", requestUri);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String token = accessToken.substring(7);
        try {
            jwtUtil.isExpired(token);
            String email = jwtUtil.getUserEmail(token);
            String role = jwtUtil.getRole(token);

            UserPersonalInfo user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

            Authentication authToken = new UsernamePasswordAuthenticationToken(
                    user, // Principal 객체로 DB에서 조회한 user 엔티티 사용
                    null,
                    Collections.singletonList(new SimpleGrantedAuthority(role))
            );

            SecurityContextHolder.getContext().setAuthentication(authToken);
            log.debug("인증 성공: userId={}, email={}, role={}", user.getUserId(), email, role);
            filterChain.doFilter(request, response);
        } catch (ExpiredJwtException e) {
            log.warn("JwtFilter - Token expired, returning 401");
            PrintWriter writer = response.getWriter();
            writer.print("access token expired");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        } catch (IllegalArgumentException e) {
            // 사용자를 찾을 수 없는 경우만 인증 오류로 처리
            if (e.getMessage().contains("사용자를 찾을 수 없습니다")) {
                log.error("JWT 인증 처리 중 오류 발생: {}", e.getMessage(), e);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            } else {
                // 다른 IllegalArgumentException은 비즈니스 로직 예외이므로 그대로 전파
                throw e;
            }
        } catch (io.jsonwebtoken.JwtException e) {
            // JWT 관련 예외만 인증 오류로 처리
            log.error("JWT 인증 처리 중 오류 발생: {}", e.getMessage(), e);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        } catch (ServletException | IOException e) {
            // filterChain.doFilter() 내부에서 발생한 ServletException/IOException은 그대로 전파
            // (이미 응답이 전송되었을 수 있음)
            throw e;
        }

       /* CustomUserDetails customUserDetails = new CustomUserDetails(userEntity);

        Authentication authToken = new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);

        filterChain.doFilter(request, response);

        */




    }

    private boolean isPublicPath(String uri) {
        return uri.equals("/api/v1/users/login") ||
                uri.equals("/api/v1/users/signup") ||
                uri.equals("/reissue") ||
                uri.startsWith("/swagger-ui") ||
                uri.startsWith("/v3/api-docs") ||
                uri.startsWith("/swagger-resources") ||
                uri.startsWith("/webjars");
    }

}
