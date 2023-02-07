package todayquest.oauth;

import io.jsonwebtoken.JwtException;
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

        String targetUrl = UriComponentsBuilder.fromUriString("/user/status")
                .build().toUriString();

        String accessToken;
        String refreshToken = jwtTokenProvider.getJwtFromRequest(request, JwtTokenProvider.REFRESH_TOKEN_NAME);

        try {
            accessToken = jwtTokenProvider.refreshAccessToken(refreshToken);
        } catch (JwtException e) {
            accessToken = jwtTokenProvider.createAccessToken(principal.getUserId());
            refreshToken = jwtTokenProvider.createRefreshToken(principal.getUserId());

            response.addCookie(jwtTokenProvider.createRefreshTokenCookie(refreshToken));
        }

        response.addCookie(jwtTokenProvider.createAccessTokenCookie(accessToken));
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}