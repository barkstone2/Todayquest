package dailyquest.jwt.service

import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.common.base.Strings
import dailyquest.common.GoogleIdTokenVerifierFactory
import dailyquest.common.MessageUtil
import dailyquest.jwt.JwtTokenProvider
import dailyquest.jwt.dto.TokenRequest
import dailyquest.redis.service.RedisService
import dailyquest.user.dto.UserSaveRequest
import dailyquest.user.service.UserService
import jakarta.servlet.http.Cookie
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Transactional
@Service
class JwtService(
    @Value("\${google.client-id}")
    private val clientId: String,
    private val jwtTokenProvider: JwtTokenProvider,
    private val userService: UserService,
    private val googleIdTokenVerifierFactory: GoogleIdTokenVerifierFactory,
    private val redisService: RedisService
) {

    fun issueTokenCookie(tokenRequest: TokenRequest): Pair<Cookie, Cookie> {

        val verifier = googleIdTokenVerifierFactory.create(NetHttpTransport(), GsonFactory.getDefaultInstance(), listOf(clientId));
        val idTokenString = tokenRequest.idToken

        if (Strings.isNullOrEmpty(idTokenString)) throw AccessDeniedException(MessageUtil.getMessage("exception.invalid.login"))

        val idToken = try { verifier.verify(idTokenString) } catch (_: Exception) { null }
            ?: throw AccessDeniedException(MessageUtil.getMessage("exception.invalid.login"))
        val oauth2Id = idToken.payload.subject
        val providerType = tokenRequest.providerType

        var foundUser = userService.findUserByOauthId(oauth2Id)
        if (foundUser == null) {
            var randomNickname = redisService.createRandomNickname()
            while (userService.isDuplicatedNickname(randomNickname)) {
                randomNickname = redisService.createRandomNickname()
            }
            val userSaveRequest = UserSaveRequest(oauth2Id, randomNickname, providerType)
            val savedUserId = userService.saveUser(userSaveRequest)
            foundUser = userService.getUserById(savedUserId)
        }

        val accessToken = jwtTokenProvider.createAccessToken(foundUser.id)
        val refreshToken = jwtTokenProvider.createRefreshToken(foundUser.id)

        return jwtTokenProvider.createAccessTokenCookie(accessToken) to jwtTokenProvider.createRefreshTokenCookie(refreshToken)
    }

    fun invalidateToken(cookies: Array<Cookie>?): Pair<Cookie, Cookie> {
        return jwtTokenProvider.invalidateToken(cookies)
    }

}