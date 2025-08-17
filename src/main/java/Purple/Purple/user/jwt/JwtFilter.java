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

        String accessToken = request.getHeader("Authorization");

        if (accessToken == null || !accessToken.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        accessToken = accessToken.substring(7);
        try {
            jwtUtil.isExpired(accessToken);
        } catch (ExpiredJwtException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().print("access token expired");
            return;
        }
        String userEmail = jwtUtil.getUserEmail(accessToken);
        String role = jwtUtil.getRole(accessToken);

        UserPersonalInfo userPersonalInfo = new UserPersonalInfo();
        userPersonalInfo.setEmail(userEmail);

        Authentication authToken = new UsernamePasswordAuthenticationToken(userPersonalInfo, null, List.of());

        SecurityContextHolder.getContext().setAuthentication(authToken);
        filterChain.doFilter(request, response);

       /* CustomUserDetails customUserDetails = new CustomUserDetails(userEntity);

        Authentication authToken = new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);

        filterChain.doFilter(request, response);

        */


    }



}
