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

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String[] ALLOWED_URL = {"/", "/css/**", "/js/**", "/image/**", "/error", "/auth/**"};
    private final JwtAuthorizationFilter jwtAuthorizationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf()
                    .csrfTokenRepository(new CookieCsrfTokenRepository())
                    .ignoringRequestMatchers(ALLOWED_URL)
                .and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        http.authorizeHttpRequests()
                .requestMatchers(ALLOWED_URL).permitAll()
                .anyRequest().authenticated();

        http.logout()
                .logoutUrl("/logout")
                .deleteCookies("JSESSIONID")
                .deleteCookies(JwtTokenProvider.ACCESS_TOKEN_NAME)
                .deleteCookies(JwtTokenProvider.REFRESH_TOKEN_NAME);

        http.addFilterBefore(jwtAuthorizationFilter, BasicAuthenticationFilter.class);

        return http.build();
    }
}
