package dailyquest.jwt;

import dailyquest.properties.SecurityUrlProperties;
import dailyquest.user.dto.UserPrincipal;
import dailyquest.user.service.UserService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.Cookie;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtAuthorizationFilter 유닛 테스트")
public class JwtAuthorizationFilterUnitTest {

    @MockBean
    static MockMvc mvc;
    static JwtTokenProvider jwtTokenProvider;
    static UserService userService;
    static SecurityUrlProperties securityUrlProperties;

    @BeforeAll
    static void init() {
        jwtTokenProvider = mock(JwtTokenProvider.class);
        userService = mock(UserService.class);
        securityUrlProperties = mock(SecurityUrlProperties.class);

        JwtAuthorizationFilter jwtAuthorizationFilter = new JwtAuthorizationFilter(jwtTokenProvider, userService, securityUrlProperties);
        mvc = MockMvcBuilders.standaloneSetup().addFilter(jwtAuthorizationFilter).build();
    }

    @DisplayName("인가가 필요없는 요청 url 일 경우 필터 로직을 건너뛴다")
    @Test
    public void doesNotCheckAllowedUrl() throws Exception {
        //given
        String[] allowedUrl = {"/allowed"};
        doReturn(allowedUrl).when(securityUrlProperties).getAllowedUrl();

        //when
        mvc.perform(get(allowedUrl[0]));

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

        doThrow(new JwtException("")).when(jwtTokenProvider).getJwtFromCookies(any(), any());

        //when
        MockHttpServletResponse response = mvc.perform(get(url))
                .andReturn()
                .getResponse();

        //then
        verify(jwtTokenProvider, never()).getUserIdFromToken(any());
        verifyNoInteractions(userService);
        assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_UNAUTHORIZED);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @DisplayName("토큰이 만료된 경우 사일런트 리프레쉬를 수행한다")
    @Test
    public void doSilentRefreshWhenExpiredToken() throws Exception {
        //given
        String url = "/someurl";
        doReturn(new String[]{"/allowed"}).when(securityUrlProperties).getAllowedUrl();
        doReturn(false).when(jwtTokenProvider).isValidToken(any(), any());

        Cookie mockCookie = mock(Cookie.class);
        doReturn(mockCookie).when(jwtTokenProvider).createAccessTokenCookie(any());
        doReturn(mockCookie).when(jwtTokenProvider).createRefreshTokenCookie(any());

        UserPrincipal mockUserDetail = mock(UserPrincipal.class);
        doReturn(mockUserDetail).when(userService).getUserById(any());

        //when
        mvc.perform(get(url));

        //then
        verify(jwtTokenProvider, times(1)).getJwtFromCookies(any(), eq(JwtTokenProvider.REFRESH_TOKEN_NAME));
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

        Cookie mockCookie = mock(Cookie.class);
        doReturn(mockCookie).when(jwtTokenProvider).createAccessTokenCookie(any());
        doReturn(mockCookie).when(jwtTokenProvider).createRefreshTokenCookie(any());

        UserPrincipal mockUserDetail = mock(UserPrincipal.class);
        doReturn(mockUserDetail).when(userService).getUserById(any());

        //when
        mvc.perform(get(url));

        //then
        verify(jwtTokenProvider, never()).getJwtFromCookies(any(), eq(JwtTokenProvider.REFRESH_TOKEN_NAME));
        verify(jwtTokenProvider, never()).silentRefresh(any());
        verify(jwtTokenProvider, never()).createAccessTokenCookie(any());
        verify(jwtTokenProvider, never()).createRefreshToken(any());

        assertThat(SecurityContextHolder.getContext().getAuthentication().isAuthenticated()).isTrue();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).isEqualTo(mockUserDetail);
    }

}
