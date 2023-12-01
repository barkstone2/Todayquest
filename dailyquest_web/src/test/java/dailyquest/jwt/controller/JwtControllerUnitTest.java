package dailyquest.jwt.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dailyquest.annotation.WithCustomMockUser;
import dailyquest.config.SecurityConfig;
import dailyquest.jwt.JwtAuthorizationFilter;
import dailyquest.jwt.dto.TokenRequest;
import dailyquest.jwt.service.JwtService;
import dailyquest.user.entity.ProviderType;
import jakarta.servlet.http.Cookie;
import kotlin.Pair;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SuppressWarnings("ALL")
@ExtendWith(MockitoExtension.class)
@WebMvcTest(controllers = JwtController.class,
        excludeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {SecurityConfig.class, JwtAuthorizationFilter.class})})
@WithCustomMockUser
@DisplayName("JWT 컨트롤러 유닛 테스트")
public class JwtControllerUnitTest {
    static final String URI_PREFIX = "/api/v1/auth";

    @Autowired
    MockMvc mvc;

    @MockBean
    JwtService jwtService;

    ObjectMapper om = new ObjectMapper().registerModule(new JavaTimeModule());

    @DisplayName("POST /issue 요청 시")
    @Nested
    class TestIssue {
        @DisplayName("요청 정보를 issueTokenCookie 요청에 위임한다")
        @Test
        public void delegateRequest() throws Exception {
            //given
            String url = URI_PREFIX + "/issue";
            String idToken = "idToken";
            ProviderType providerType = ProviderType.GOOGLE;
            TokenRequest mockRequest = new TokenRequest(idToken, providerType);

            Pair<Cookie, Cookie> mockPair = mock(Pair.class);
            doReturn(mockPair).when(jwtService).issueTokenCookie(eq(mockRequest));

            //when
            mvc.perform(
                    post(url)
                            .with(csrf())
                            .content(om.writeValueAsBytes(mockRequest))
                            .contentType(MediaType.APPLICATION_JSON_UTF8)
            );

            //then
            verify(jwtService).issueTokenCookie(eq(mockRequest));
        }

        @DisplayName("리스폰스에 쿠키를 담아 반환한다")
        @Test
        public void doReturnCookie() throws Exception {
            //given
            String url = URI_PREFIX + "/issue";

            String idToken = "idToken";
            ProviderType providerType = ProviderType.GOOGLE;
            TokenRequest mockRequest = new TokenRequest(idToken, providerType);

            Cookie access = mock(Cookie.class);
            Cookie refresh = mock(Cookie.class);

            Pair<Cookie, Cookie> pair = new Pair<>(access, refresh);
            doReturn(pair).when(jwtService).issueTokenCookie(eq(mockRequest));

            //when
            MockHttpServletResponse response = mvc.perform(
                            post(url)
                                    .with(csrf())
                                    .content(om.writeValueAsBytes(mockRequest))
                                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    ).andReturn()
                    .getResponse();

            //then
            assertThat(response.getCookies()).contains(access, refresh);
        }

    }

    @DisplayName("POST /logout 요청 시")
    @Nested
    class TestLogout {
        @DisplayName("invalidateToken 메서드에 요청을 위임한다")
        @Test
        public void delegateTo() throws Exception {
            //given
            String url = URI_PREFIX + "/logout";

            Pair<Cookie, Cookie> mockPair = mock(Pair.class);
            doReturn(mockPair).when(jwtService).invalidateToken(any());

            //when
            mvc.perform(
                    post(url)
                            .with(csrf())
            );

            //then
            verify(jwtService).invalidateToken(any());
        }

        @DisplayName("response 에 쿠키가 담겨 반환된다")
        @Test
        public void returnCookie() throws Exception {
            //given
            String url = URI_PREFIX + "/logout";

            Cookie access = mock(Cookie.class);
            Cookie refresh = mock(Cookie.class);

            Pair<Cookie, Cookie> pair = new Pair<>(access, refresh);
            doReturn(pair).when(jwtService).invalidateToken(any());

            //when
            MockHttpServletResponse response = mvc.perform(
                            post(url)
                                    .with(csrf())
                    ).andReturn()
                    .getResponse();

            //then
            assertThat(response.getCookies()).contains(access, refresh);
        }
    }

}
