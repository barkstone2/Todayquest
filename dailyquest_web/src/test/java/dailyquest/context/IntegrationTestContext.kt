package dailyquest.context

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import dailyquest.jwt.JwtTokenProvider
import dailyquest.user.entity.ProviderType
import dailyquest.user.entity.RoleType
import dailyquest.user.entity.User
import dailyquest.user.repository.UserRepository
import jakarta.servlet.http.Cookie
import org.junit.jupiter.api.BeforeEach
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers
import org.springframework.test.web.servlet.MockHttpServletRequestDsl
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.filter.CharacterEncodingFilter

@Transactional
class IntegrationTestContext(
    protected val context: WebApplicationContext,
    protected val userRepository: UserRepository,
    protected val jwtTokenProvider: JwtTokenProvider,
) {
    companion object {
        const val SERVER_ADDR = "http://localhost:"
        val om: ObjectMapper = ObjectMapper().registerModule(JavaTimeModule()).registerKotlinModule()
    }

    @LocalServerPort
    var port = 0

    protected lateinit var mvc: MockMvc
    protected lateinit var userToken: Cookie
    protected lateinit var anotherUserToken: Cookie
    protected lateinit var adminToken: Cookie
    protected lateinit var user: User
    protected lateinit var anotherUser: User
    protected lateinit var admin: User

    @BeforeEach
    fun baseSetup() {
        mvc = MockMvcBuilders
            .webAppContextSetup(context)
            .addFilter<DefaultMockMvcBuilder>(CharacterEncodingFilter("UTF-8", true))
            .apply<DefaultMockMvcBuilder>(SecurityMockMvcConfigurers.springSecurity())
            .build()

        user = User("user", "user", ProviderType.GOOGLE)
        anotherUser = User("anotherUser", "anotherUser", ProviderType.GOOGLE)
        admin = User("admin", "admin", ProviderType.GOOGLE)
        admin.role = RoleType.ADMIN

        user = userRepository.save(user)
        anotherUser = userRepository.save(anotherUser)
        admin = userRepository.save(admin)

        val userAccessToken = jwtTokenProvider.createAccessToken(user.id)
        userToken = jwtTokenProvider.createAccessTokenCookie(userAccessToken)

        val anotherUserAccessToken = jwtTokenProvider.createAccessToken(anotherUser.id)
        anotherUserToken = jwtTokenProvider.createAccessTokenCookie(anotherUserAccessToken)

        val adminAccessToken = jwtTokenProvider.createAccessToken(admin.id)
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

    fun MockHttpServletRequestDsl.useAdminConfiguration() {
        this.useBaseConfiguration()
        this.cookie(adminToken)
    }

    fun MockHttpServletRequestDsl.useUserConfiguration() {
        this.useBaseConfiguration()
        this.cookie(userToken)
    }

    fun MockHttpServletRequestDsl.useAnotherUserConfiguration() {
        this.useBaseConfiguration()
        this.cookie(anotherUserToken)
    }

    private fun MockHttpServletRequestDsl.useBaseConfiguration() {
        this.contentType = MediaType.APPLICATION_JSON
        this.with(SecurityMockMvcRequestPostProcessors.csrf())
    }
}