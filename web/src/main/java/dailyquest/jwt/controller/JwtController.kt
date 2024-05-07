package dailyquest.jwt.controller

import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import dailyquest.jwt.dto.TokenRequest
import dailyquest.jwt.service.JwtService
import jakarta.servlet.http.HttpServletRequest

@RequestMapping("/api/v1/auth")
@RestController
class JwtController(
    private val jwtService: JwtService,
) {

    @PostMapping("/issue")
    fun issueToken(@RequestBody tokenRequest: TokenRequest, response: HttpServletResponse) {
        val (accessTokenCookie, refreshTokenCookie) = jwtService.issueTokenCookie(tokenRequest)
        response.addCookie(accessTokenCookie)
        response.addCookie(refreshTokenCookie)
    }

    @PostMapping("/logout")
    fun invalidateToken(response: HttpServletResponse, request: HttpServletRequest) {
        val (emptyAccessToken, emptyRefreshToken) = jwtService.invalidateToken(request.cookies)
        response.addCookie(emptyAccessToken)
        response.addCookie(emptyRefreshToken)
    }

}