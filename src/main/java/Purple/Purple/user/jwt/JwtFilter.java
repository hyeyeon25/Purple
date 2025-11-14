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
            log.warn("JwtFilter - No valid Authorization header, returning 401");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String token = accessToken.substring(7);
        try {
            jwtUtil.isExpired(token);
        } catch (ExpiredJwtException e) {
            log.warn("JwtFilter - Token expired, returning 401");
            PrintWriter writer = response.getWriter();
            writer.print("access token expired");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String email = jwtUtil.getUserEmail(token);
        String role = jwtUtil.getRole(token);
        log.info("JwtFilter - Email: {}, Role: {}", email, role);

        UserPersonalInfo user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("JwtFilter - User not found for email: {}", email);
                    return new IllegalArgumentException("사용자를 찾을 수 없습니다.");
                });

        log.info("JwtFilter - User found: userId={}", user.getUserId());

        // ROLE_ prefix 추가 (Spring Security 표준)
        String roleWithPrefix = role.startsWith("ROLE_") ? role : "ROLE_" + role;
        log.info("JwtFilter - Authority set as: {}", roleWithPrefix);

        Authentication authToken = new UsernamePasswordAuthenticationToken(
                user,
                null,
                Collections.singletonList(new SimpleGrantedAuthority(roleWithPrefix))
        );

        SecurityContextHolder.getContext().setAuthentication(authToken);
        log.info("JwtFilter - Authentication set successfully, proceeding with filter chain");
        filterChain.doFilter(request, response);

       /* CustomUserDetails customUserDetails = new CustomUserDetails(userEntity);

        Authentication authToken = new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);

        filterChain.doFilter(request, response);

        */




    }

    private boolean isPublicPath(String uri) {
        return uri.matches("^/api/v1/users/login$") ||
                uri.matches("^/api/v1/users/signup$") ||
                uri.matches("^/reissue$") ||
                uri.matches("^(/swagger-ui|/v3/api-docs|/swagger-resources|/webjars).*$");
    }

}
