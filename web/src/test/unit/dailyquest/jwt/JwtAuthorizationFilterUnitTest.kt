package dailyquest.jwt

import dailyquest.jwt.dto.SilentRefreshResult
import dailyquest.properties.JwtTokenProperties
import dailyquest.properties.SecurityUrlProperties
import dailyquest.redis.service.RedisService
import dailyquest.user.dto.UserPrincipal
import dailyquest.user.dto.UserResponse
import dailyquest.user.entity.RoleType
import dailyquest.user.service.UserService
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import jakarta.servlet.FilterChain
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.security.core.context.SecurityContextHolder

@ExtendWith(MockKExtension::class)
@DisplayName("JwtAuthorizationFilter 유닛 테스트")
class JwtAuthorizationFilterUnitTest {
    @InjectMockKs
    lateinit var jwtAuthorizationFilter: JwtAuthorizationFilter
    @RelaxedMockK
    lateinit var jwtTokenProvider: JwtTokenProvider
    @RelaxedMockK
    lateinit var userService: UserService
    @RelaxedMockK
    lateinit var securityUrlProperties: SecurityUrlProperties
    @RelaxedMockK
    lateinit var request: HttpServletRequest
    @RelaxedMockK
    lateinit var response: HttpServletResponse
    @RelaxedMockK
    lateinit var filterChain: FilterChain
    @RelaxedMockK
    lateinit var jwtTokenProperties: JwtTokenProperties
    @RelaxedMockK
    lateinit var redisService: RedisService
    private val accessTokenName: String = "access"
    private val refreshTokenName: String = "refresh"

    @BeforeEach
    fun init() {
        SecurityContextHolder.clearContext()
        every { jwtTokenProperties.accessTokenName } returns accessTokenName
        every { jwtTokenProperties.refreshTokenName } returns refreshTokenName
    }

    @DisplayName("shouldNotFilter 호출 시")
    @Nested
    inner class TestShouldNotFilter {
        @DisplayName("internalUrl 중 requestURI와 패턴이 일치하는 것이 있다면 true를 반환한다")
        @Test
        fun `internalUrl 중 requestURI와 패턴이 일치하는 것이 있다면 true를 반환한다`() {
            //given
            every { request.requestURI } returns "/A/B"
            every { securityUrlProperties.internalUrl } returns arrayOf("/A/**")

            //when
            val result = jwtAuthorizationFilter.shouldNotFilter(request)

            //then
            assertThat(result).isTrue()
        }

        @DisplayName("allowedUrl 중 requestURI와 패턴이 일치하는 것이 있다면 true를 반환한다")
        @Test
        fun `allowedUrl 중 requestURI와 패턴이 일치하는 것이 있다면 true를 반환한다`() {
            //given
            every { request.requestURI } returns "/A/B"
            every { securityUrlProperties.allowedUrl } returns arrayOf("/A/**")

            //when
            val result = jwtAuthorizationFilter.shouldNotFilter(request)

            //then
            assertThat(result).isTrue()
        }

        @DisplayName("internalUrl과 allowedUrl 모두 requestURI와 패턴이 일치하지 않으면 false를 반환한다")
        @Test
        fun `internalUrl과 allowedUrl 모두 requestURI와 패턴이 일치하지 않으면 false를 반환한다`() {
            //given
            every { request.requestURI } returns "/A/B"
            every { securityUrlProperties.allowedUrl } returns arrayOf("/B/**")
            every { securityUrlProperties.internalUrl } returns arrayOf("/C/**")

            //when
            val result = jwtAuthorizationFilter.shouldNotFilter(request)

            //then
            assertThat(result).isFalse()
        }
    }

    @DisplayName("doFilterInternal 호출 시")
    @Nested
    inner class TestDoFilterInternal {
        @DisplayName("acessToken을 조회한다")
        @Test
        fun `acessToken을 조회한다`() {
            //given
            //when
            jwtAuthorizationFilter.doFilterInternal(request, response, filterChain)

            //then
            verify { jwtTokenProvider.getJwtFromCookies(any(), accessTokenName) }
        }

        @DisplayName("조회한 토큰이 유효하지 않으면 silentRefresh가 호출된다")
        @Test
        fun `조회한 토큰이 유효하지 않으면 silentRefresh가 호출된다`() {
            //given
            every { jwtTokenProvider.isValidToken(any(), accessTokenName) } returns false

            //when
            jwtAuthorizationFilter.doFilterInternal(request, response, filterChain)

            //then
            verify { jwtTokenProvider.silentRefresh(any()) }
        }

        @DisplayName("조회한 토큰이 유효하면 silentRefresh가 호출되지 않는다")
        @Test
        fun `조회한 토큰이 유효하면 silentRefresh가 호출되지 않는다`() {
            //given
            every { jwtTokenProvider.isValidToken(any(), accessTokenName) } returns true

            //when
            jwtAuthorizationFilter.doFilterInternal(request, response, filterChain)

            //then
            verify(inverse = true) { jwtTokenProvider.silentRefresh(any()) }
        }

        @DisplayName("토큰에 담긴 userId로 유저 정보를 조회한다")
        @Test
        fun `토큰에 담긴 userId로 유저 정보를 조회한다`() {
            //given
            val userId = 1L
            every { jwtTokenProvider.getUserIdFromToken(any()) } returns userId
            val userResponse = mockk<UserResponse>(relaxed = true)
            every { userService.getUserById(any()) } returns userResponse
            every { userResponse.role } returns RoleType.USER

            //when
            jwtAuthorizationFilter.doFilterInternal(request, response, filterChain)

            //then
            verify { userService.getUserById(eq(userId)) }
        }

        @DisplayName("조회한 유저 정보로 SecurityContext에 인증 정보를 담는다")
        @Test
        fun `조회한 유저 정보로 SecurityContext에 인증 정보를 담는다`() {
            //given
            val userResponse: UserResponse = mockk(relaxed = true)
            every { userService.getUserById(any()) } returns userResponse
            mockkObject(UserPrincipal)
            val userPrincipal: UserPrincipal = mockk(relaxed = true)
            every { UserPrincipal.from(any(), any()) } returns userPrincipal

            //when
            jwtAuthorizationFilter.doFilterInternal(request, response, filterChain)

            //then
            val authentication = SecurityContextHolder.getContext().authentication
            assertThat(authentication.principal).isEqualTo(userPrincipal)
        }
    }

    @DisplayName("doSilentRefresh 호출 시")
    @Nested
    inner class TestSilentRefresh {
        @DisplayName("refresh 토큰을 조회한다")
        @Test
        fun `refresh 토큰을 조회한다`() {
            //given
            //when
            jwtAuthorizationFilter.doSilentRefresh(request, response)

            //then
            verify { jwtTokenProvider.getJwtFromCookies(any(), refreshTokenName) }
        }

        @DisplayName("조회한 refresh 토큰으로 silentRefresh를 요청한다")
        @Test
        fun `조회한 refresh 토큰으로 silentRefresh를 요청한다`() {
            //given
            val refreshToken = "refreshToken"
            every { jwtTokenProvider.getJwtFromCookies(any(), refreshTokenName) } returns refreshToken
            
            //when
            jwtAuthorizationFilter.doSilentRefresh(request, response)
            
            //then
            verify { jwtTokenProvider.silentRefresh(refreshToken) }
        }

        @DisplayName("새로 발급받은 accessToken으로 쿠키를 만들어 response에 담는다")
        @Test
        fun `새로 발급받은 accessToken으로 쿠키를 만들어 response에 담는다`() {
            //given
            val accessToken = "newAccessToken"
            val refreshToken = "newRefreshToken"
            val silentRefreshResult = SilentRefreshResult(accessToken, refreshToken)
            every { jwtTokenProvider.silentRefresh(any()) } returns silentRefreshResult
            val accessTokenCookie = mockk<Cookie>()
            every { jwtTokenProvider.createAccessTokenCookie(eq(accessToken)) } returns accessTokenCookie

            //when
            jwtAuthorizationFilter.doSilentRefresh(request, response)

            //then
            verify { response.addCookie(accessTokenCookie) }
        }

        @DisplayName("새로 발급받은 refreshToken으로 쿠키를 만들어 response에 담는다")
        @Test
        fun `새로 발급받은 refreshToken으로 쿠키를 만들어 response에 담는다`() {
            //given
            val accessToken = "newAccessToken"
            val refreshToken = "newRefreshToken"
            val silentRefreshResult = SilentRefreshResult(accessToken, refreshToken)
            every { jwtTokenProvider.silentRefresh(any()) } returns silentRefreshResult
            val refreshTokenCookie = mockk<Cookie>()
            every { jwtTokenProvider.createRefreshTokenCookie(eq(refreshToken)) } returns refreshTokenCookie

            //when
            jwtAuthorizationFilter.doSilentRefresh(request, response)

            //then
            verify { response.addCookie(refreshTokenCookie) }
        }
    }
}