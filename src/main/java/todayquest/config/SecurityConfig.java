package todayquest.config;

import lombok.RequiredArgsConstructor;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import todayquest.oauth.service.CustomOAuth2UserService;
import todayquest.oauth.service.CustomOidcUserService;

@RequiredArgsConstructor
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private static final String[] ALLOWED_URL = {"/oauth-login", "/"};
    private final CustomOAuth2UserService customOAuth2UserService;
    private final CustomOidcUserService customOidcUserService;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http

                .authorizeRequests()
                .antMatchers(ALLOWED_URL).permitAll() // login URL에는 누구나 접근 가능하게 합니다.
                .anyRequest().authenticated() // 그 이외에는 인증된 사용자만 접근 가능하게 합니다.
                .and()
                .oauth2Login() // oauth2Login 설정 시작
                .loginPage("/oauth-login")
                .userInfoEndpoint() // oauth2Login 성공 이후의 설정을 시작
                .oidcUserService(customOidcUserService)
                .userService(customOAuth2UserService);
    }
}
