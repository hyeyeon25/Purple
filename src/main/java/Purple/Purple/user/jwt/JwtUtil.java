package Purple.Purple.user.jwt;

import Purple.Purple.user.entity.RefreshEntity;
import Purple.Purple.user.repository.RefreshRepository;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secretKey;

    private final RefreshRepository refreshRepository;

    private Key getSigningKey() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }


    public String createJwt(String category, String email, String role, Long expiredMs) {
        return createJwt(category, email, role, null, expiredMs);
    }

    public String createJwt(String category, String email, String role, Long userId, Long expiredMs) {
        var builder = Jwts.builder()
                .setSubject(String.valueOf(userId != null ? userId : email))  // userId를 subject로 설정
                .claim("category", category)
                .claim("email", email)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiredMs));
        
        if (userId != null) {
            builder.claim("userId", userId);
        }
        
        return builder.signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String getCategory(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("category", String.class);
    }

    public String getUserEmail(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("email", String.class);
    }

    public String getRole(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("role", String.class);
    }

    public Date getExpiration(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getExpiration();


    }

    public Long getUserIdFromToken(String token) {
        var claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        
        // userId claim에서 가져오기
        Object userIdObj = claims.get("userId");
        if (userIdObj != null) {
            if (userIdObj instanceof Long) {
                return (Long) userIdObj;
            } else if (userIdObj instanceof Integer) {
                return ((Integer) userIdObj).longValue();
            } else if (userIdObj instanceof String) {
                return Long.valueOf((String) userIdObj);
            }
        }
        
        // Subject에서 가져오기 (하위 호환성)
        String subject = claims.getSubject();
        if (subject != null && !subject.isEmpty()) {
            return Long.valueOf(subject);
        }
        
        return null;
    }


    public void isExpired(String token) {
        Date exp = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getExpiration();

        if (exp.before(new Date())) {
            throw new ExpiredJwtException(null, null, "Token expired");
        }
    }

    public boolean validate(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void addRefreshEntity(String username, String refresh, Long expiredMs) {

        Date date = new Date(System.currentTimeMillis() + expiredMs);

        RefreshEntity refreshEntity = new RefreshEntity();
        refreshEntity.setUsername(username);
        refreshEntity.setRefresh(refresh);
        refreshEntity.setExpiration(date.toString());

        refreshRepository.save(refreshEntity);
    }
}
