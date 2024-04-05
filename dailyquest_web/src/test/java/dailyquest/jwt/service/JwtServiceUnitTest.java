package dailyquest.jwt.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import dailyquest.common.GoogleIdTokenVerifierFactory;
import dailyquest.common.MessageUtil;
import dailyquest.jwt.JwtTokenProvider;
import dailyquest.jwt.dto.TokenRequest;
import dailyquest.redis.service.RedisService;
import dailyquest.user.dto.UserResponse;
import dailyquest.user.entity.ProviderType;
import dailyquest.user.service.UserService;
import jakarta.servlet.http.Cookie;
import kotlin.Pair;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JWT 서비스 유닛 테스트")
public class JwtServiceUnitTest {

    JwtService jwtService;
    @Mock JwtTokenProvider jwtTokenProvider;
    @Mock UserService userService;
    @Mock GoogleIdTokenVerifierFactory verifierFactory;
    @Mock RedisService redisService;

    MockedStatic<MessageUtil> messageUtil;
    String clientId = "test-client-id";

    @BeforeEach
    void before() {
        jwtService = new JwtService(clientId, jwtTokenProvider, userService, verifierFactory, redisService);
        messageUtil = mockStatic(MessageUtil.class);
        when(MessageUtil.getMessage(any())).thenReturn("");
        when(MessageUtil.getMessage(any(), any())).thenReturn("");
    }

    @AfterEach
    void clear() {
        messageUtil.close();
    }

    @DisplayName("issueTokenCookie 요청 시")
    @Nested
    class TestIssueTokenCookie {
        @DisplayName("idToken 이 null 이라면 AccessDenied 예외를 던진다")
        @Test
        public void throwAccessDeniedWhenIdTokenIsNull() throws Exception {
            //given
            TokenRequest mockRequest = mock(TokenRequest.class);

            //when
            Runnable run = () -> jwtService.issueTokenCookie(mockRequest);

            //then
            assertThatThrownBy(run::run).isInstanceOf(AccessDeniedException.class);
        }

        @DisplayName("verify 실패 시 AccessDenied 예외를 던진다")
        @Test
        public void throwAccessDeniedWhenVerifyFailed() throws Exception {
            //given
            TokenRequest mockRequest = mock(TokenRequest.class);
            String idToken = "id-token";
            doReturn(idToken).when(mockRequest).getIdToken();

            GoogleIdTokenVerifier mockVerifier = mock(GoogleIdTokenVerifier.class);
            doReturn(mockVerifier).when(verifierFactory).create(any(), any(), any());
            doThrow(new RuntimeException()).when(mockVerifier).verify(eq(idToken));

            //when
            Runnable run = () -> jwtService.issueTokenCookie(mockRequest);

            //then
            assertThatThrownBy(run::run).isInstanceOf(AccessDeniedException.class);
        }

        @DisplayName("idToken이 유효하고")
        @Nested
        class WhenIdTokenValid {
            @DisplayName("가입된 유저 정보가 있으면 유저 아이디로 토큰을 생성해 반환한다")
            @Test
            public void whenUserExistThenCreateTokenByUserId() throws Exception {
                //given
                TokenRequest mockRequest = mock(TokenRequest.class);

                String rawIdToken = "id-token";
                doReturn(rawIdToken).when(mockRequest).getIdToken();

                GoogleIdTokenVerifier mockVerifier = mock(GoogleIdTokenVerifier.class);
                doReturn(mockVerifier).when(verifierFactory).create(any(), any(), any());

                GoogleIdToken mockIdToken = mock(GoogleIdToken.class);
                doReturn(mockIdToken).when(mockVerifier).verify(eq(rawIdToken));

                GoogleIdToken.Payload mockPayload = mock(GoogleIdToken.Payload.class, Answers.RETURNS_SMART_NULLS);
                doReturn(mockPayload).when(mockIdToken).getPayload();

                UserResponse mockUser = mock(UserResponse.class);
                doReturn(mockUser).when(userService).findUserByOauthId(any());

                String accessToken = "access";
                String refreshToken = "refresh";

                doReturn(accessToken).when(jwtTokenProvider).createAccessToken(any());
                doReturn(refreshToken).when(jwtTokenProvider).createRefreshToken(any());

                //when
                jwtService.issueTokenCookie(mockRequest);

                //then
                verify(jwtTokenProvider).createAccessToken(any());
                verify(jwtTokenProvider).createRefreshToken(any());

                verify(jwtTokenProvider).createAccessTokenCookie(eq(accessToken));
                verify(jwtTokenProvider).createRefreshTokenCookie(eq(refreshToken));
            }

            @DisplayName("가입된 유저 정보가 없으면 유저 정보를 새로 등록한다")
            @Test
            public void whenUserDoesNotExistThenSaveUser() throws Exception {
                //given
                TokenRequest mockRequest = mock(TokenRequest.class);
                doReturn(ProviderType.GOOGLE).when(mockRequest).getProviderType();

                String rawIdToken = "id-token";
                doReturn(rawIdToken).when(mockRequest).getIdToken();

                GoogleIdTokenVerifier mockVerifier = mock(GoogleIdTokenVerifier.class);
                doReturn(mockVerifier).when(verifierFactory).create(any(), any(), any());

                GoogleIdToken mockIdToken = mock(GoogleIdToken.class);
                doReturn(mockIdToken).when(mockVerifier).verify(eq(rawIdToken));

                GoogleIdToken.Payload mockPayload = mock(GoogleIdToken.Payload.class, Answers.RETURNS_SMART_NULLS);
                doReturn(mockPayload).when(mockIdToken).getPayload();

                doReturn(null).when(userService).findUserByOauthId(any());

                String accessToken = "access";
                String refreshToken = "refresh";

                doReturn(accessToken).when(jwtTokenProvider).createAccessToken(any());
                doReturn(refreshToken).when(jwtTokenProvider).createRefreshToken(any());

                doReturn("").when(redisService).createRandomNickname();
                UserResponse mockUser = mock(UserResponse.class);
                doReturn(mockUser).when(userService).getUserById(anyLong());

                //when
                jwtService.issueTokenCookie(mockRequest);

                //then
                verify(userService).saveUser(any());
            }
        }
    }

    @DisplayName("invalidateToken 요청 시")
    @Nested
    class TestInvalidateToken {
        @DisplayName("인자로 넘어온 쿠키로 요청을 위임한다")
        @Test
        public void passParameterTo() throws Exception {
            //given
            Cookie mockCookie = mock(Cookie.class);
            Cookie[] cookies = new Cookie[]{mockCookie};

            Pair mockPair = mock(Pair.class);
            doReturn(mockPair).when(jwtTokenProvider).invalidateToken(any());

            //when
            jwtService.invalidateToken(cookies);

            //then
            verify(jwtTokenProvider).invalidateToken(eq(cookies));
        }

    }

}
