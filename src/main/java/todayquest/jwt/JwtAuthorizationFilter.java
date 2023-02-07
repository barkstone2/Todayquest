package todayquest.jwt;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import todayquest.user.service.UserService;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
@Component
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();
    private final List<String> allowedUrls = List.of("/", "/css/**", "/js/**", "/image/**", "/error", "/user/login");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String requestUri = request.getRequestURI();

        if (allowedUrls.stream().anyMatch(url -> antPathMatcher.match(url, requestUri))) {
            filterChain.doFilter(request, response);
            return;
        }

        try {

            String accessToken = jwtTokenProvider.getJwtFromRequest(request, JwtTokenProvider.ACCESS_TOKEN_NAME);
            if(!jwtTokenProvider.isValidToken(accessToken)) {
                String refreshToken = jwtTokenProvider.getJwtFromRequest(request, JwtTokenProvider.REFRESH_TOKEN_NAME);
                accessToken = jwtTokenProvider.refreshAccessToken(refreshToken);
                response.addCookie(jwtTokenProvider.createAccessTokenCookie(accessToken));
            }

            Long userId = jwtTokenProvider.getUserIdFromToken(accessToken);

            UserDetails userDetails = userService.getUserInfoById(userId);
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (JwtException ex) {
            request.setAttribute("message", "로그인 정보가 만료되었어요. 다시 로그인 해주세요.");
            response.sendRedirect("/logout");
        }

        filterChain.doFilter(request, response);
    }

}
