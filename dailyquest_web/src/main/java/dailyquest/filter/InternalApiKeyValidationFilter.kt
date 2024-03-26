package dailyquest.filter

import dailyquest.properties.SecurityKeyProperties
import dailyquest.properties.SecurityUrlProperties
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.apache.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.util.AntPathMatcher
import org.springframework.web.filter.OncePerRequestFilter

@Component
class InternalApiKeyValidationFilter(
    private val securityUrlProperties: SecurityUrlProperties,
    private val securityKeyProperties: SecurityKeyProperties
): OncePerRequestFilter() {
    private val antPathMatcher = AntPathMatcher()

    override fun shouldNotFilterAsyncDispatch(): Boolean {
        return false
    }

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        val requestUri = request.requestURI
        return !securityUrlProperties.internalUrl.any { url -> antPathMatcher.match(url, requestUri) }
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val apiKey: String? = request.getHeader("Internal-API-Key")
        if (!apiKey.equals(securityKeyProperties.internalApiKey)) {
            response.sendError(HttpStatus.SC_FORBIDDEN, "권한이 없습니다.")
            return
        }
        filterChain.doFilter(request, response)
    }
}