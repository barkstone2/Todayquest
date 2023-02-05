package todayquest.oauth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import todayquest.jwt.JwtTokenProvider;
import todayquest.user.dto.UserPrincipal;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();

        String accessToken = jwtTokenProvider.createAccessToken(principal.getUserId());
        String refreshToken = jwtTokenProvider.createRefreshToken();

        response.addCookie(jwtTokenProvider.createAccessTokenCookie(accessToken));
        response.addCookie(jwtTokenProvider.createRefreshTokenCookie(refreshToken));

        String targetUrl;
        log.info("토큰 발행 시작");

        targetUrl = UriComponentsBuilder.fromUriString("/user/status")
                .queryParam(JwtTokenProvider.ACCESS_TOKEN_NAME, accessToken)
                .build().toUriString();
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}