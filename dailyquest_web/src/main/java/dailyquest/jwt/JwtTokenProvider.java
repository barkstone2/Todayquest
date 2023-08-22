package dailyquest.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import jakarta.annotation.Nullable;
import jakarta.servlet.http.Cookie;
import kotlin.Pair;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import dailyquest.common.MessageUtil;

import javax.crypto.spec.SecretKeySpec;
import java.time.Duration;
import java.util.Date;

@RequiredArgsConstructor
@Component
public class JwtTokenProvider {

    public final static long ACCESS_TOKEN_VALIDATION_MILLISECOND = 1000L * 60 * 30;
    public final static long REFRESH_TOKEN_VALIDATION_MILLISECOND = 1000L * 60 * 60 * 24 * 14;
    public final static String ACCESS_TOKEN_NAME = "X-ACCESS-TOKEN";
    public final static String REFRESH_TOKEN_NAME = "X-REFRESH-TOKEN";
    private final RedisTemplate<String, String> redisTemplate;

    @Value("${spring.jwt.secret}")
    private String secretKey;

    public String createAccessToken(Long userPk) {
        Date now = new Date();
        return Jwts.builder()
                .claim("id", userPk)
                .claim("token_type", ACCESS_TOKEN_NAME)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + ACCESS_TOKEN_VALIDATION_MILLISECOND))
                .signWith(new SecretKeySpec(Decoders.BASE64.decode(secretKey), SignatureAlgorithm.HS256.getJcaName()))
                .compact();

    }

    public String createRefreshToken(Long userId) {
        Date now = new Date();

        return Jwts.builder()
                .claim("id", userId)
                .claim("token_type", REFRESH_TOKEN_NAME)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + REFRESH_TOKEN_VALIDATION_MILLISECOND))
                .signWith(new SecretKeySpec(Decoders.BASE64.decode(secretKey), SignatureAlgorithm.HS256.getJcaName()))
                .compact();
    }

    public boolean isValidToken(String jwtToken, String tokenType) {
        try {
            Jws<Claims> claims = Jwts.parserBuilder()
                    .setSigningKey(Decoders.BASE64.decode(secretKey))
                    .build()
                    .parseClaimsJws(jwtToken);

            Claims body = claims.getBody();
            String token_type = body.get("token_type", String.class);

            return token_type.equals(tokenType);
        } catch (Exception e) {
            return false;
        }
    }

    public Long getUserIdFromToken(String jwtToken) throws ExpiredJwtException {

        Jws<Claims> claims = Jwts.parserBuilder()
                .setSigningKey(Decoders.BASE64.decode(secretKey))
                .build()
                .parseClaimsJws(jwtToken);

        return claims.getBody().get("id", Long.class);
    }

    public Date getExpiredDateFromToken(String jwtToken) throws ExpiredJwtException {
        Jws<Claims> claims = Jwts.parserBuilder()
                .setSigningKey(Decoders.BASE64.decode(secretKey))
                .build()
                .parseClaimsJws(jwtToken);

        return claims.getBody().getExpiration();
    }

    public String getJwtFromCookies(@Nullable Cookie[] cookies, String tokenType){

        if(cookies == null) throw new JwtException(MessageUtil.getMessage("exception.invalid.login"));

        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(tokenType))
                return cookie.getValue();
        }

        throw new JwtException(MessageUtil.getMessage("exception.invalid.login"));
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

    public String silentRefresh(String refreshToken) throws JwtException {
        try {
            if (!isInBlackList(refreshToken) && isValidToken(refreshToken, REFRESH_TOKEN_NAME)) {
                Long userId = getUserIdFromToken(refreshToken);
                addToBlackList(refreshToken);

                return createAccessToken(userId);
            } else {
                throw new JwtException("로그인 시간이 만료됐습니다. 다시 로그인 해주세요.");
            }
        } catch (Exception e) {
            throw new JwtException("로그인 시간이 만료됐습니다. 다시 로그인 해주세요.");
        }
    }

    public Pair<Cookie, Cookie> invalidateToken(@Nullable Cookie[] cookies) throws ExpiredJwtException {
        String refreshToken = getJwtFromCookies(cookies, REFRESH_TOKEN_NAME);

        // 토큰 만료 시 블랙 리스트 추가 불필요
        try {
            addToBlackList(refreshToken);
        } catch (ExpiredJwtException ignored) {
        }

        Cookie emptyAccessToken = new Cookie(JwtTokenProvider.ACCESS_TOKEN_NAME, "");
        Cookie emptyRefreshToken = new Cookie(JwtTokenProvider.REFRESH_TOKEN_NAME, "");

        emptyAccessToken.setMaxAge(0);
        emptyRefreshToken.setMaxAge(0);

        emptyAccessToken.setPath("/");
        emptyRefreshToken.setPath("/");

        emptyAccessToken.setHttpOnly(true);
        emptyRefreshToken.setHttpOnly(true);

        return new Pair<>(emptyAccessToken, emptyRefreshToken);
    }

    private void addToBlackList(String refreshToken) throws ExpiredJwtException {
        Date expiredDate = getExpiredDateFromToken(refreshToken);

        long epochSecond = expiredDate.toInstant().getEpochSecond();
        long now = new Date().toInstant().getEpochSecond();

        redisTemplate.opsForValue().set(refreshToken, "", Duration.ofSeconds(epochSecond - now));
    }

    private boolean isInBlackList(String token) {
        return redisTemplate.opsForValue().get(token) != null;
    }

}
