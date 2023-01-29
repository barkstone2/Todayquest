package todayquest.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import todayquest.oauth.service.CustomOAuth2UserService;
import todayquest.oauth.service.CustomOidcUserService;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String[] ALLOWED_URL = {"/", "/css/**", "/js/**", "/image/**"};
    private final CustomOAuth2UserService customOAuth2UserService;
    private final CustomOidcUserService customOidcUserService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests()
                .requestMatchers(ALLOWED_URL).permitAll()
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

        return http.build();
    }
}
