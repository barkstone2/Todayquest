package dailyquest.jwt;

import dailyquest.jwt.dto.SilentRefreshResult;
import dailyquest.properties.JwtTokenProperties;
import dailyquest.properties.SecurityUrlProperties;
import dailyquest.redis.service.RedisService;
import dailyquest.user.dto.UserPrincipal;
import dailyquest.user.dto.UserResponse;
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
import java.util.Map;

@RequiredArgsConstructor
@Component
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private final JwtTokenProperties jwtTokenProperties;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();
    private final SecurityUrlProperties securityUrlProperties;
    private final RedisService redisService;

    @Override
    protected boolean shouldNotFilterAsyncDispatch() {
        return false;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        String[] allowedUrl = securityUrlProperties.getAllowedUrl();
        return Arrays.stream(allowedUrl).anyMatch(url -> antPathMatcher.match(url, requestUri));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            String accessToken = jwtTokenProvider.getJwtFromCookies(request.getCookies(), jwtTokenProperties.getAccessTokenName());
            if(!jwtTokenProvider.isValidToken(accessToken, jwtTokenProperties.getAccessTokenName())) {
                SilentRefreshResult silentRefreshResult = this.doSilentRefresh(request, response);
                accessToken = silentRefreshResult.getAccessToken();
            }
            this.parseAndSetAuthentication(accessToken);
        } catch (JwtException ex) {
            response.sendError(HttpStatus.SC_UNAUTHORIZED, "로그인 정보가 만료되었어요. 다시 로그인 해주세요.");
            return;
        }

        filterChain.doFilter(request, response);
    }

    protected SilentRefreshResult doSilentRefresh(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = jwtTokenProvider.getJwtFromCookies(request.getCookies(), jwtTokenProperties.getRefreshTokenName());
        SilentRefreshResult result = jwtTokenProvider.silentRefresh(refreshToken);
        response.addCookie(jwtTokenProvider.createAccessTokenCookie(result.getAccessToken()));
        response.addCookie(jwtTokenProvider.createRefreshTokenCookie(result.getRefreshToken()));
        return result;
    }

    protected void parseAndSetAuthentication(String accessToken) {
        Long userId = jwtTokenProvider.getUserIdFromToken(accessToken);
        UserResponse userResponse = userService.getUserById(userId);
        Map<Integer, Long> expTable = redisService.getExpTable();
        UserPrincipal userDetails = UserPrincipal.from(userResponse, expTable);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
