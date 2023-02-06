package todayquest.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import todayquest.jwt.JwtAuthorizationFilter;
import todayquest.jwt.JwtTokenProvider;
import todayquest.oauth.CustomOAuth2UserService;
import todayquest.oauth.CustomOidcUserService;
import todayquest.oauth.OAuth2SuccessHandler;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String[] ALLOWED_URL = {"/", "/css/**", "/js/**", "/image/**"};
    private final CustomOAuth2UserService customOAuth2UserService;
    private final CustomOidcUserService customOidcUserService;
    private final JwtAuthorizationFilter jwtAuthorizationFilter;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf()
                    .csrfTokenRepository(new CookieCsrfTokenRepository())
                .and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        http.authorizeHttpRequests()
                .requestMatchers(ALLOWED_URL).permitAll()
                .anyRequest().authenticated();

        http.logout()
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .deleteCookies("JSESSIONID")
                .deleteCookies(JwtTokenProvider.ACCESS_TOKEN_NAME);

        http
                .oauth2Login() // oauth2Login 설정 시작
                .loginPage("/")
                .successHandler(oAuth2SuccessHandler)
                .userInfoEndpoint() // oauth2Login 성공 이후의 설정을 시작
                .oidcUserService(customOidcUserService)
                .userService(customOAuth2UserService);

        http.addFilterBefore(jwtAuthorizationFilter, BasicAuthenticationFilter.class); // 토큰 체크 필터 추가

        return http.build();
    }
}
