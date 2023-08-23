package dailyquest.jwt;

import dailyquest.common.MessageUtil;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import jakarta.servlet.http.Cookie;
import kotlin.Pair;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.spec.SecretKeySpec;
import java.time.Duration;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JWT 토큰 프로바이더 단위 테스트")
public class JwtTokenProviderUnitTest {

    @Mock
    RedisTemplate<String, String> redisTemplate;

    @InjectMocks
    JwtTokenProvider jwtTokenProvider;

    String secretKey = "and0LWF1dGhlbnRpY2F0aW9uLWZpbHRlci10ZXN0LXNlY3JldC1rZXQtMDAwMA==";
    String anotherKey = "xxand0LWF1dGhlbnRpY2F0aW9uLWZpbHRlci10ZXN0LXNlY3JldC1rZXQtMDAwMA==";

    static MockedStatic<MessageUtil> messageUtil;

    @BeforeAll
    static void beforeAll() {
        messageUtil = mockStatic(MessageUtil.class);
        when(MessageUtil.getMessage(any())).thenReturn("");
        when(MessageUtil.getMessage(any(), any())).thenReturn("");
    }

    @BeforeEach
    void before() {
        ReflectionTestUtils.setField(jwtTokenProvider, "secretKey", secretKey);
    }


    @DisplayName("createAccessToken 요청 시")
    @Nested
    class TestCreateAccessToken {

        @DisplayName("반환된 토큰에 파라미터에 전달된 ID가 포함되어야 한다")
        @Test
        public void haveToIncludeId() throws Exception {
            //given
            Long userId = 3L;

            //when
            String accessToken = jwtTokenProvider.createAccessToken(userId);

            //then
            Long userIdFromToken = jwtTokenProvider.getUserIdFromToken(accessToken);
            assertThat(userId).isEqualTo(userIdFromToken);
        }
        
        @DisplayName("반환된 토큰 타입이 엑세스 토큰이어야 한다")
        @Test
        public void haveToBeAccessToken() throws Exception {
            //given
            Long userId = 5L;

            //when
            String accessToken = jwtTokenProvider.createAccessToken(userId);

            //then
            Jws<Claims> claims = Jwts.parserBuilder()
                    .setSigningKey(Decoders.BASE64.decode(secretKey))
                    .build()
                    .parseClaimsJws(accessToken);

            Claims body = claims.getBody();
            String token_type = body.get("token_type", String.class);

            assertThat(token_type).isEqualTo(JwtTokenProvider.ACCESS_TOKEN_NAME);
        }

        @DisplayName("올바른 시크릿 키를 사용해서 생성된 토큰이 반환되야 한다")
        @Test
        public void haveToUseValidSecretKey() throws Exception {
            //given
            Long userId = 1L;
            JwtParser invalidParser = Jwts.parserBuilder()
                    .setSigningKey(Decoders.BASE64.decode(anotherKey))
                    .build();
            JwtParser validParser = Jwts.parserBuilder()
                    .setSigningKey(Decoders.BASE64.decode(secretKey))
                    .build();

            //when
            String accessToken = jwtTokenProvider.createAccessToken(userId);

            //then
            Runnable invalidRun = () -> invalidParser.parseClaimsJws(accessToken);
            Runnable validRun = () -> validParser.parseClaimsJws(accessToken);

            assertThatThrownBy(invalidRun::run);
            assertDoesNotThrow(validRun::run);
        }

        @DisplayName("토큰 생성 시간이 요청 시간이며 올바른 만료 시간이 지정돼야 한다")
        @Test
        public void haveToBeCreatedAtInvoked() throws Exception {
            //given
            Long userId = 9L;
            Date now = new Date();
            Date expiredDate = new Date(now.getTime() + JwtTokenProvider.ACCESS_TOKEN_VALIDATION_MILLISECOND);

            //when
            String accessToken = jwtTokenProvider.createAccessToken(userId);

            //then
            Jws<Claims> claims = Jwts.parserBuilder()
                    .setSigningKey(Decoders.BASE64.decode(secretKey))
                    .build()
                    .parseClaimsJws(accessToken);

            Claims body = claims.getBody();

            Date issuedAt = body.getIssuedAt();
            Date expiration = body.getExpiration();

            assertThat(issuedAt).isCloseTo(now, 1000 * 10);
            assertThat(expiration).isCloseTo(expiredDate, 1000 * 10);
        }
    }

    @DisplayName("createRefreshToken 요청 시")
    @Nested
    class TestCreateRefreshToken {
        @DisplayName("반환된 토큰에 파라미터에 전달된 ID가 포함되어야 한다")
        @Test
        public void haveToIncludeId() throws Exception {
            //given
            Long userId = 3L;

            //when
            String refreshToken = jwtTokenProvider.createRefreshToken(userId);

            //then
            Long userIdFromToken = jwtTokenProvider.getUserIdFromToken(refreshToken);
            assertThat(userId).isEqualTo(userIdFromToken);
        }

        @DisplayName("반환된 토큰 타입이 리프레시 토큰이어야 한다")
        @Test
        public void haveToBeRefreshToken() throws Exception {
            //given
            Long userId = 5L;

            //when
            String refreshToken = jwtTokenProvider.createRefreshToken(userId);

            //then
            Jws<Claims> claims = Jwts.parserBuilder()
                    .setSigningKey(Decoders.BASE64.decode(secretKey))
                    .build()
                    .parseClaimsJws(refreshToken);

            Claims body = claims.getBody();
            String token_type = body.get("token_type", String.class);

            assertThat(token_type).isEqualTo(JwtTokenProvider.REFRESH_TOKEN_NAME);
        }

        @DisplayName("올바른 시크릿 키를 사용해서 생성된 토큰이 반환되야 한다")
        @Test
        public void haveToUseValidSecretKey() throws Exception {
            //given
            Long userId = 1L;
            JwtParser invalidParser = Jwts.parserBuilder()
                    .setSigningKey(Decoders.BASE64.decode(anotherKey))
                    .build();
            JwtParser validParser = Jwts.parserBuilder()
                    .setSigningKey(Decoders.BASE64.decode(secretKey))
                    .build();

            //when
            String refreshToken = jwtTokenProvider.createRefreshToken(userId);

            //then
            Runnable invalidRun = () -> invalidParser.parseClaimsJws(refreshToken);
            Runnable validRun = () -> validParser.parseClaimsJws(refreshToken);

            assertThatThrownBy(invalidRun::run);
            assertDoesNotThrow(validRun::run);
        }

        @DisplayName("토큰 생성 시간이 요청 시간이며 올바른 만료 시간이 지정돼야 한다")
        @Test
        public void haveToBeCreatedAtInvoked() throws Exception {
            //given
            Long userId = 9L;
            Date now = new Date();
            Date expiredDate = new Date(now.getTime() + JwtTokenProvider.REFRESH_TOKEN_VALIDATION_MILLISECOND);

            //when
            String refreshToken = jwtTokenProvider.createRefreshToken(userId);

            //then
            Jws<Claims> claims = Jwts.parserBuilder()
                    .setSigningKey(Decoders.BASE64.decode(secretKey))
                    .build()
                    .parseClaimsJws(refreshToken);

            Claims body = claims.getBody();

            Date issuedAt = body.getIssuedAt();
            Date expiration = body.getExpiration();

            assertThat(issuedAt).isCloseTo(now, 1000 * 10);
            assertThat(expiration).isCloseTo(expiredDate, 1000 * 10);
        }
    }


    @DisplayName("isValidToken 요청 시")
    @Nested
    class IsValidTokenTest {
        @DisplayName("유효하지 않은 토큰일 경우 false가 반환된다")
        @Test
        public void doReturnFalseWhenInvalid() throws Exception {
            //given
            String invalidSecretKey = "asdsadqwlkdqnmlkqw===1=as12e21==134124adasd12==4214=";
            String invalidToken = Jwts.builder()
                    .claim("token_type", JwtTokenProvider.REFRESH_TOKEN_NAME)
                    .signWith(new SecretKeySpec(Decoders.BASE64.decode(invalidSecretKey), SignatureAlgorithm.HS256.getJcaName()))
                    .compact();

            //when
            boolean isValidToken = jwtTokenProvider.isValidToken(invalidToken, JwtTokenProvider.REFRESH_TOKEN_NAME);

            //then
            assertThat(isValidToken).isFalse();
        }

        @DisplayName("요청 타입과 실제 타입이 다를 경우 false 를 반환한다")
        @Test
        public void doReturnFalseWhenIncorrectType() throws Exception {
            //given
            String validToken = jwtTokenProvider.createRefreshToken(1L);

            //when
            boolean isValidToken = jwtTokenProvider.isValidToken(validToken, JwtTokenProvider.ACCESS_TOKEN_NAME);

            //then
            assertThat(isValidToken).isFalse();
        }

        @DisplayName("만료된 토큰일 경우 false 를 반환한다")
        @Test
        public void doReturnFalseWhenExpired() throws Exception {
            //given
            String expiredToken = Jwts.builder()
                    .claim("token_type", JwtTokenProvider.REFRESH_TOKEN_NAME)
                    .setExpiration(new Date())
                    .signWith(new SecretKeySpec(Decoders.BASE64.decode(secretKey), SignatureAlgorithm.HS256.getJcaName()))
                    .compact();

            //when
            boolean isValidToken = jwtTokenProvider.isValidToken(expiredToken, JwtTokenProvider.REFRESH_TOKEN_NAME);

            //then
            assertThat(isValidToken).isFalse();
        }
        
        @DisplayName("유효한 토큰일 경우 true가 반환된다")
        @Test
        public void doReturnTrueWhenValid() throws Exception {
            //given
            String validToken = jwtTokenProvider.createRefreshToken(1L);

            //when
            boolean isValidToken = jwtTokenProvider.isValidToken(validToken, JwtTokenProvider.REFRESH_TOKEN_NAME);

            //then
            assertThat(isValidToken).isTrue();
        }
    }

    @DisplayName("createAccessTokenCookie 요청 시 httpOnly, path=/ 인 쿠키가 반환된다")
    @Test
    public void testCreateAccessTokenCookie() throws Exception {
        //given
        String accessToken = jwtTokenProvider.createAccessToken(1L);

        //when
        Cookie accessTokenCookie = jwtTokenProvider.createAccessTokenCookie(accessToken);

        //then
        assertThat(accessTokenCookie.isHttpOnly()).isTrue();
        assertThat(accessTokenCookie.getPath()).isEqualTo("/");
    }
    
    @DisplayName("createRefreshTokenCookie 요청 시 httpOnly, path=/ 인 쿠키가 반환된다")
    @Test
    public void testCreateRefreshTokenCookie() throws Exception {
        //given
        String refreshToken = jwtTokenProvider.createRefreshToken(1L);

        //when
        Cookie refreshTokenCookie = jwtTokenProvider.createRefreshTokenCookie(refreshToken);

        //then
        assertThat(refreshTokenCookie.isHttpOnly()).isTrue();
        assertThat(refreshTokenCookie.getPath()).isEqualTo("/");
    }

    @DisplayName("silentRefresh 요청 시")
    @Nested
    class SilentRefreshTest {
        @DisplayName("요청에 사용된 리프레시 토큰이 블랙 리스트에 포함된 토큰이라면 예외를 던진다")
        @Test
        public void doThrowWhenInBlackList() throws Exception {
            //given
            ValueOperations<String, String> mockOperation = mock(ValueOperations.class);
            doReturn(mockOperation).when(redisTemplate).opsForValue();
            doReturn("in black list").when(mockOperation).get(any());

            String token = jwtTokenProvider.createRefreshToken(1L);

            //when
            Runnable runnable = () -> jwtTokenProvider.silentRefresh(token);

            //then
            assertThatThrownBy(runnable::run).isInstanceOf(JwtException.class);
        }

        @DisplayName("유효하지 않은 토큰이라면 예외를 던진다")
        @Test
        public void doThrowWhenInvalidToken() throws Exception {
            //given
            ValueOperations<String, String> mockOperation = mock(ValueOperations.class);
            doReturn(mockOperation).when(redisTemplate).opsForValue();
            doReturn(null).when(mockOperation).get(any());

            String invalidSecretKey = "asdsadqwlkdqnmlkqw===1=as12e21==134124adasd12==4214=";
            String invalidToken = Jwts.builder()
                    .claim("token_type", JwtTokenProvider.REFRESH_TOKEN_NAME)
                    .signWith(new SecretKeySpec(Decoders.BASE64.decode(invalidSecretKey), SignatureAlgorithm.HS256.getJcaName()))
                    .compact();

            //when
            Runnable runnable = () -> jwtTokenProvider.silentRefresh(invalidToken);

            //then
            assertThatThrownBy(runnable::run).isInstanceOf(JwtException.class);
        }

        @DisplayName("유효한 토큰이라면 리프레시 토큰을 블랙 리스트 처리 후 새 엑세스 토큰을 반환한다")
        @Test
        public void doRefresh() throws Exception {
            //given
            ValueOperations<String, String> mockOperation = mock(ValueOperations.class);
            doReturn(mockOperation).when(redisTemplate).opsForValue();
            doReturn(null).when(mockOperation).get(any());

            String token = jwtTokenProvider.createRefreshToken(1L);

            //when
            String newToken = jwtTokenProvider.silentRefresh(token);

            //then
            verify(mockOperation).set(eq(token), eq(""), any(Duration.class));
            assertThat(newToken).isNotEmpty();
            assertThat(newToken).isNotEqualTo(token);
        }
    }
    @DisplayName("토큰 invalidate 요청 시")
    @Nested
    class TokenInvalidateTest {

        @DisplayName("이미 만료된 토큰일 경우 빈 토큰을 반환한다")
        @Test
        public void doReturnEmptyTokenWhenExpired() throws Exception {
            //given
            String expiredRefreshToken = Jwts.builder()
                    .claim("token_type", JwtTokenProvider.REFRESH_TOKEN_NAME)
                    .setExpiration(new Date())
                    .signWith(new SecretKeySpec(Decoders.BASE64.decode(secretKey), SignatureAlgorithm.HS256.getJcaName()))
                    .compact();

            Cookie[] cookies = new Cookie[]{jwtTokenProvider.createRefreshTokenCookie(expiredRefreshToken)};

            //when
            Pair<Cookie, Cookie> emptyCookies = jwtTokenProvider.invalidateToken(cookies);

            //then
            verify(redisTemplate, never()).opsForValue();
            assertThat(emptyCookies.getFirst().getMaxAge()).isEqualTo(0);
            assertThat(emptyCookies.getFirst().getValue()).isEmpty();
            assertThat(emptyCookies.getSecond().getMaxAge()).isEqualTo(0);
            assertThat(emptyCookies.getSecond().getValue()).isEmpty();
        }

        @DisplayName("리프레쉬 토큰을 레디스 블랙 리스트에 등록한다")
        @Test
        public void doSaveOnRedis() throws Exception {
            //given
            String refreshToken = jwtTokenProvider.createRefreshToken(1L);
            Cookie[] cookies = new Cookie[]{jwtTokenProvider.createRefreshTokenCookie(refreshToken)};

            ValueOperations<String, String> mockOperation = mock(ValueOperations.class);
            doReturn(mockOperation).when(redisTemplate).opsForValue();

            //when
            jwtTokenProvider.invalidateToken(cookies);

            //then
            verify(mockOperation).set(eq(refreshToken), eq(""), any(Duration.class));
        }

        @DisplayName("레디스에 등록된 토큰에 토큰 만료 시간만큼의 TTL이 등록된다")
        @Test
        public void doSetTTLOnRedis() throws Exception {
            //given
            ValueOperations<String, String> mockOperation = mock(ValueOperations.class);
            doReturn(mockOperation).when(redisTemplate).opsForValue();

            String refreshToken = jwtTokenProvider.createRefreshToken(1L);
            Cookie[] cookies = new Cookie[]{jwtTokenProvider.createRefreshTokenCookie(refreshToken)};

            Date expiredDate = jwtTokenProvider.getExpiredDateFromToken(refreshToken);
            long now = new Date().toInstant().getEpochSecond();
            Duration timeout = Duration.ofSeconds(expiredDate.toInstant().getEpochSecond() - now);

            ArgumentCaptor<Duration> durationCaptor = ArgumentCaptor.forClass(Duration.class);

            //when
            jwtTokenProvider.invalidateToken(cookies);

            //then
            verify(mockOperation).set(eq(refreshToken), eq(""), durationCaptor.capture());
            assertThat(durationCaptor.getValue()).isCloseTo(timeout, Duration.ofSeconds(600));
        }

        @DisplayName("쿠키에 토큰 정보가 없을 경우 예외를 던지지 않고 빈 토큰을 반환한다")
        @Test
        public void doesNotThrowWhenTokenNull() throws Exception {
            //given
            Cookie[] cookies = null;

            //when
            Runnable run = () -> jwtTokenProvider.invalidateToken(cookies);

            //then
            assertDoesNotThrow(run::run);
        }

    }
}
