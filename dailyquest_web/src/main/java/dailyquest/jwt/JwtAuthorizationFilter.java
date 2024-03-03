package dailyquest.jwt;

import dailyquest.properties.JwtTokenProperties;
import dailyquest.properties.SecurityUrlProperties;
import dailyquest.user.dto.UserPrincipal;
import dailyquest.user.service.UserService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

@RequiredArgsConstructor
@Component
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private final JwtTokenProperties jwtTokenProperties;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();
    private final SecurityUrlProperties securityUrlProperties;

    @Override
    protected boolean shouldNotFilterAsyncDispatch() {
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String requestUri = request.getRequestURI();

        if (Arrays.stream(securityUrlProperties.getAllowedUrl()).anyMatch(url -> antPathMatcher.match(url, requestUri))) {
            filterChain.doFilter(request, response);
            return;
        }

        try {

            String accessToken = jwtTokenProvider.getJwtFromCookies(request.getCookies(), jwtTokenProperties.getAccessTokenName());
            if(!jwtTokenProvider.isValidToken(accessToken, jwtTokenProperties.getAccessTokenName())) {
                String refreshToken = jwtTokenProvider.getJwtFromCookies(request.getCookies(), jwtTokenProperties.getRefreshTokenName());

                accessToken = jwtTokenProvider.silentRefresh(refreshToken);
                response.addCookie(jwtTokenProvider.createAccessTokenCookie(accessToken));

                String newRefreshToken = jwtTokenProvider.createRefreshToken(jwtTokenProvider.getUserIdFromToken(refreshToken));
                response.addCookie(jwtTokenProvider.createRefreshTokenCookie(newRefreshToken));
            }

            Long userId = jwtTokenProvider.getUserIdFromToken(accessToken);

            UserPrincipal userDetails = userService.getUserPrincipal(userId);
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (JwtException ex) {
            response.sendError(HttpStatus.SC_UNAUTHORIZED, "로그인 정보가 만료되었어요. 다시 로그인 해주세요.");
            return;
        }

        filterChain.doFilter(request, response);
    }

}
