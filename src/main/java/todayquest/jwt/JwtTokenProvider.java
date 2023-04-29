package todayquest.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.util.Date;

@RequiredArgsConstructor
@Component
public class JwtTokenProvider {

    public final static long ACCESS_TOKEN_VALIDATION_SECOND = 1000L * 60 * 30;
    public final static long REFRESH_TOKEN_VALIDATION_SECOND = 1000L * 60 * 60 * 24 * 14;
    public final static String ACCESS_TOKEN_NAME = "X-ACCESS-TOKEN";
    public final static String REFRESH_TOKEN_NAME = "X-REFRESH-TOKEN";
    private final RedisTemplate<String, Long> redisTemplate;

    @Value("${spring.jwt.secret}")
    private String secretKey;

    public String createAccessToken(Long userPk) {
        Date now = new Date();
        return Jwts.builder()
                .claim("id", userPk)
                .claim("token_type", ACCESS_TOKEN_NAME)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + ACCESS_TOKEN_VALIDATION_SECOND))
                .signWith(new SecretKeySpec(Decoders.BASE64.decode(secretKey), SignatureAlgorithm.HS256.getJcaName()))
                .compact();

    }

    public String createRefreshToken(Long userId) {
        Date now = new Date();

        return Jwts.builder()
                .claim("id", userId)
                .claim("token_type", REFRESH_TOKEN_NAME)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + REFRESH_TOKEN_VALIDATION_SECOND))
                .signWith(new SecretKeySpec(Decoders.BASE64.decode(secretKey), SignatureAlgorithm.HS256.getJcaName()))
                .compact();
    }

    public boolean isValidToken(String jwtToken) {
        try {
            Jws<Claims> claims = Jwts.parserBuilder()
                    .setSigningKey(Decoders.BASE64.decode(secretKey))
                    .build()
                    .parseClaimsJws(jwtToken);
            return !claims.getBody().getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    public long getUserIdFromToken(String jwtToken) throws ExpiredJwtException {

        Jws<Claims> claims = Jwts.parserBuilder()
                .setSigningKey(Decoders.BASE64.decode(secretKey))
                .build()
                .parseClaimsJws(jwtToken);

        return claims.getBody().get("id", Long.class);
    }

    public String getJwtFromRequest(HttpServletRequest request, String tokenType){
        Cookie[] cookies = request.getCookies();
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(tokenType))
                return cookie.getValue();
        }
        return null;
    }

    public Cookie createAccessTokenCookie(String accessToken) {
        Cookie cookie = new Cookie(JwtTokenProvider.ACCESS_TOKEN_NAME, accessToken);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        return cookie;
    }

    public Cookie createRefreshTokenCookie(String refreshToken) {
        Cookie cookie = new Cookie(JwtTokenProvider.REFRESH_TOKEN_NAME, refreshToken);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        return cookie;
    }

    public String refreshAccessToken(String refreshToken) throws JwtException {

        if (isValidToken(refreshToken)) {
            Long userId = getUserIdFromRedis(refreshToken);
            return createAccessToken(userId);
        } else {
            deleteRefreshToken(refreshToken);
            throw new JwtException("로그인 시간이 만료됐습니다. 다시 로그인 해주세요.");
        }
    }

    private void storeRefreshToken(String refreshToken, Long userId) {
        redisTemplate.opsForValue().set(refreshToken, userId);
    }

    private void deleteRefreshToken(String refreshToken) {
        redisTemplate.delete(refreshToken);
    }

    private Long getUserIdFromRedis(String refreshToken) {
        return redisTemplate.opsForValue().get(refreshToken);
    }

}
