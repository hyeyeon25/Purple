package Purple.Purple.user.jwt;

import Purple.Purple.user.entity.UserPersonalInfo;
import Purple.Purple.user.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
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
        try {
            String token = resolveToken(request);

            if (token != null && jwtUtil.validateToken(token)) {
                Long userId = jwtUtil.getUserIdFromToken(token);
                UserPersonalInfo user = userRepository.findById(userId).orElse(null);

                if (user != null) {
                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(
                                    user, null, List.of() // 권한 필요 시 추가
                            );
                    SecurityContextHolder.getContext().setAuthentication(auth);
                    log.debug("사용자 인증 성공: {}", user.getEmail());
                }
            }
        } catch (Exception e) {
            log.error("JWT 필터 처리 중 예외 발생: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        return (bearer != null && bearer.startsWith("Bearer ")) ? bearer.substring(7) : null;
    }
}
