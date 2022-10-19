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

    private static final String[] ALLOWED_URL = {"/", "/css/**", "/js/**", "/image/**"};
    private final CustomOAuth2UserService customOAuth2UserService;
    private final CustomOidcUserService customOidcUserService;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers(ALLOWED_URL).permitAll()
                .anyRequest().authenticated();

        http.logout()
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID");

        http.oauth2Login() // oauth2Login 설정 시작
                .loginPage("/")
                .userInfoEndpoint() // oauth2Login 성공 이후의 설정을 시작
                .oidcUserService(customOidcUserService)
                .userService(customOAuth2UserService)
                .and().defaultSuccessUrl("/user/status");

    }
}
