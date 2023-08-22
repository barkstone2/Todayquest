package dailyquest.jwt.service

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.common.base.Strings
import jakarta.servlet.http.Cookie
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import dailyquest.common.MessageUtil
import dailyquest.jwt.JwtTokenProvider
import dailyquest.jwt.dto.TokenRequest
import dailyquest.user.service.UserService

@Transactional
@Service
class JwtService(
    @Value("\${google.client-id}")
    private val clientId: String,
    private val jwtTokenProvider: JwtTokenProvider,
    private val userService: UserService,
) {

    fun issueTokenCookie(tokenRequest: TokenRequest): Pair<Cookie, Cookie> {

        val verifier = GoogleIdTokenVerifier.Builder(NetHttpTransport(), GsonFactory.getDefaultInstance())
            .setAudience(listOf(clientId))
            .build()
        val idTokenString = tokenRequest.idToken

        if (Strings.isNullOrEmpty(idTokenString)) throw AccessDeniedException(MessageUtil.getMessage("exception.invalid.login"))

        val idToken = verifier.verify(idTokenString)
            ?: throw AccessDeniedException(MessageUtil.getMessage("exception.invalid.login"))
        val oauth2Id = idToken.payload.subject
        val providerType = tokenRequest.providerType

        val userPrincipal = userService.getOrRegisterUser(oauth2Id, providerType)

        val accessToken = jwtTokenProvider.createAccessToken(userPrincipal.id)
        val refreshToken = jwtTokenProvider.createRefreshToken(userPrincipal.id)

        return jwtTokenProvider.createAccessTokenCookie(accessToken) to jwtTokenProvider.createRefreshTokenCookie(refreshToken)
    }

    fun invalidateToken(cookies: Array<Cookie>?): Pair<Cookie, Cookie> {
        return jwtTokenProvider.invalidateToken(cookies)
    }

}