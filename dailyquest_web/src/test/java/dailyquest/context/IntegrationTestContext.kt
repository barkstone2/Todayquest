package dailyquest.context

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import dailyquest.jwt.JwtTokenProvider
import dailyquest.user.entity.ProviderType
import dailyquest.user.entity.RoleType
import dailyquest.user.entity.UserInfo
import dailyquest.user.repository.UserRepository
import jakarta.servlet.http.Cookie
import org.junit.jupiter.api.BeforeEach
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.filter.CharacterEncodingFilter

open class IntegrationTestContext(
    private val context: WebApplicationContext,
    private val userRepository: UserRepository,
    private val jwtTokenProvider: JwtTokenProvider,
) {
    companion object {
        const val SERVER_ADDR = "http://localhost:"
        val om: ObjectMapper = ObjectMapper().registerModule(JavaTimeModule()).registerKotlinModule()
    }

    @LocalServerPort
    var port = 0

    lateinit var mvc: MockMvc
    private lateinit var userToken: Cookie
    private lateinit var anotherUserToken: Cookie
    private lateinit var adminToken: Cookie

    @BeforeEach
    fun baseSetup() {
        mvc = MockMvcBuilders
            .webAppContextSetup(context)
            .addFilter<DefaultMockMvcBuilder>(CharacterEncodingFilter("UTF-8", true))
            .apply<DefaultMockMvcBuilder>(SecurityMockMvcConfigurers.springSecurity())
            .build()

        val user = UserInfo("user", "user", ProviderType.GOOGLE)
        val anotherUser = UserInfo("anotherUser", "anotherUser", ProviderType.GOOGLE)
        val admin = UserInfo("admin", "admin", ProviderType.GOOGLE)
        admin.role = RoleType.ADMIN

        val savedUser = userRepository.save(user)
        val savedAnotherUser = userRepository.save(anotherUser)
        val savedAdmin = userRepository.save(admin)

        val userAccessToken = jwtTokenProvider.createAccessToken(savedUser.id)
        userToken = jwtTokenProvider.createAccessTokenCookie(userAccessToken)

        val anotherUserAccessToken = jwtTokenProvider.createAccessToken(savedAnotherUser.id)
        anotherUserToken = jwtTokenProvider.createAccessTokenCookie(anotherUserAccessToken)

        val adminAccessToken = jwtTokenProvider.createAccessToken(savedAdmin.id)
        adminToken = jwtTokenProvider.createAccessTokenCookie(adminAccessToken)
    }

    fun MockHttpServletRequestBuilder.useAdminConfiguration(): MockHttpServletRequestBuilder {
        return this.useBaseConfiguration()
            .cookie(adminToken)
    }

    fun MockHttpServletRequestBuilder.useUserConfiguration(): MockHttpServletRequestBuilder {
        return this.useBaseConfiguration()
            .cookie(userToken)
    }

    fun MockHttpServletRequestBuilder.useAnotherUserConfiguration(): MockHttpServletRequestBuilder {
        return this.useBaseConfiguration()
            .cookie(anotherUserToken)
    }

    private fun MockHttpServletRequestBuilder.useBaseConfiguration(): MockHttpServletRequestBuilder {
        return this.contentType(MediaType.APPLICATION_JSON)
            .with(SecurityMockMvcRequestPostProcessors.csrf())
    }
}