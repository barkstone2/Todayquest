package todayquest.jwt;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import todayquest.properties.SecurityUrlProperties;
import todayquest.user.service.UserService;

import java.io.IOException;
import java.util.Arrays;

@RequiredArgsConstructor
@Component
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();
    private final SecurityUrlProperties securityUrlProperties;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String requestUri = request.getRequestURI();

        if (Arrays.stream(securityUrlProperties.getAllowedUrl()).anyMatch(url -> antPathMatcher.match(url, requestUri))) {
            filterChain.doFilter(request, response);
            return;
        }

        try {

            String accessToken = jwtTokenProvider.getJwtFromCookies(request.getCookies(), JwtTokenProvider.ACCESS_TOKEN_NAME);
            if(!jwtTokenProvider.isValidToken(accessToken, JwtTokenProvider.ACCESS_TOKEN_NAME)) {
                String refreshToken = jwtTokenProvider.getJwtFromCookies(request.getCookies(), JwtTokenProvider.REFRESH_TOKEN_NAME);

                accessToken = jwtTokenProvider.silentRefresh(refreshToken);
                response.addCookie(jwtTokenProvider.createAccessTokenCookie(accessToken));

                String newRefreshToken = jwtTokenProvider.createRefreshToken(jwtTokenProvider.getUserIdFromToken(refreshToken));
                response.addCookie(jwtTokenProvider.createRefreshTokenCookie(newRefreshToken));
            }

            Long userId = jwtTokenProvider.getUserIdFromToken(accessToken);

            UserDetails userDetails = userService.getUserById(userId);
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (JwtException ex) {
            response.sendError(HttpStatus.SC_UNAUTHORIZED, "로그인 정보가 만료되었어요. 다시 로그인 해주세요.");
            return;
        }

        filterChain.doFilter(request, response);
    }

}
