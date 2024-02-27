package dailyquest.jwt;

import dailyquest.properties.JwtTokenProperties;
import dailyquest.properties.SecurityUrlProperties;
import dailyquest.user.dto.UserPrincipal;
import dailyquest.user.service.UserService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtAuthorizationFilter 유닛 테스트")
public class JwtAuthorizationFilterUnitTest {

    @InjectMocks JwtAuthorizationFilter jwtAuthorizationFilter;
    @Mock JwtTokenProvider jwtTokenProvider;
    @Mock UserService userService;
    @Mock SecurityUrlProperties securityUrlProperties;
    @Mock HttpServletRequest request;
    @Mock HttpServletResponse response;
    @Mock FilterChain filterChain;
    @Mock JwtTokenProperties jwtTokenProperties;
    String REFRESH_TOKEN_NAME = "refresh";

    @BeforeEach
    void init() {
        SecurityContextHolder.clearContext();
    }

    @DisplayName("인가가 필요없는 요청 url 일 경우 필터 로직을 건너뛴다")
    @Test
    public void doesNotCheckAllowedUrl() throws Exception {
        //given
        String[] allowedUrl = {"/allowed"};
        doReturn(allowedUrl).when(securityUrlProperties).getAllowedUrl();
        doReturn(allowedUrl[0]).when(request).getRequestURI();

        //when
        jwtAuthorizationFilter.doFilterInternal(request, response, filterChain);

        //then
        verifyNoInteractions(jwtTokenProvider, userService);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @DisplayName("토큰 조회 및 처리 실패 시 401 에러 리스폰스를 반환한다")
    @Test
    public void sendErrorWhenFailToGetToken() throws Exception {
        //given
        String url = "/someurl";
        doReturn(new String[]{"/allowed"}).when(securityUrlProperties).getAllowedUrl();
        doReturn(url).when(request).getRequestURI();
        doThrow(new JwtException("")).when(jwtTokenProvider).getJwtFromCookies(any(), any());

        //when
        jwtAuthorizationFilter.doFilterInternal(request, response, filterChain);

        //then
        verify(jwtTokenProvider, times(0)).getUserIdFromToken(any());
        verifyNoInteractions(userService);
        verify(response).sendError(eq(HttpServletResponse.SC_UNAUTHORIZED), any());
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @DisplayName("토큰이 만료된 경우 사일런트 리프레쉬를 수행한다")
    @Test
    public void doSilentRefreshWhenExpiredToken() throws Exception {
        //given
        String url = "/someurl";
        doReturn(new String[]{"/allowed"}).when(securityUrlProperties).getAllowedUrl();
        doReturn(false).when(jwtTokenProvider).isValidToken(any(), any());
        doReturn(url).when(request).getRequestURI();
        doReturn(REFRESH_TOKEN_NAME).when(jwtTokenProperties).getRefreshTokenName();

        Cookie mockCookie = mock(Cookie.class);
        doReturn(mockCookie).when(jwtTokenProvider).createAccessTokenCookie(any());
        doReturn(mockCookie).when(jwtTokenProvider).createRefreshTokenCookie(any());

        UserPrincipal mockUserDetail = mock(UserPrincipal.class);
        doReturn(mockUserDetail).when(userService).getUserPrincipal(any());

        //when
        jwtAuthorizationFilter.doFilterInternal(request, response, filterChain);

        //then
        verify(jwtTokenProvider, times(1)).getJwtFromCookies(any(), eq(REFRESH_TOKEN_NAME));
        verify(jwtTokenProvider, times(1)).silentRefresh(any());
        verify(jwtTokenProvider, times(1)).createAccessTokenCookie(any());
        verify(jwtTokenProvider, times(1)).createRefreshToken(any());
        assertThat(SecurityContextHolder.getContext().getAuthentication().isAuthenticated()).isTrue();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).isEqualTo(mockUserDetail);
    }

    @DisplayName("인증에 성공한 경우 Authentication 정보를 등록한다")
    @Test
    public void setAuthenticationWhenSuccess() throws Exception {
        //given
        String url = "/someurl";
        doReturn(new String[]{"/allowed"}).when(securityUrlProperties).getAllowedUrl();
        doReturn(true).when(jwtTokenProvider).isValidToken(any(), any());
        doReturn(url).when(request).getRequestURI();

        UserPrincipal mockUserDetail = mock(UserPrincipal.class);
        doReturn(mockUserDetail).when(userService).getUserPrincipal(any());

        //when
        jwtAuthorizationFilter.doFilterInternal(request, response, filterChain);

        //then
        verify(jwtTokenProvider, never()).getJwtFromCookies(any(), eq(REFRESH_TOKEN_NAME));
        verify(jwtTokenProvider, never()).silentRefresh(any());
        verify(jwtTokenProvider, never()).createAccessTokenCookie(any());
        verify(jwtTokenProvider, never()).createRefreshToken(any());

        assertThat(SecurityContextHolder.getContext().getAuthentication().isAuthenticated()).isTrue();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).isEqualTo(mockUserDetail);
    }

}
