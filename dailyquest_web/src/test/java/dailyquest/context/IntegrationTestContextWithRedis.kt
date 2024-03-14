package dailyquest.context

import dailyquest.common.CustomRedisContainer
import dailyquest.jwt.JwtTokenProvider
import dailyquest.user.repository.UserRepository
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.context.annotation.Import
import org.springframework.web.context.WebApplicationContext
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
@ExtendWith(MockKExtension::class)
@Import(MockElasticsearchTestContextConfig::class)
class IntegrationTestContextWithRedis(
    context: WebApplicationContext,
    userRepository: UserRepository,
    jwtTokenProvider: JwtTokenProvider,
) : IntegrationTestContext(context, userRepository, jwtTokenProvider) {
    companion object {
        @JvmStatic
        @Container
        val redis = CustomRedisContainer()

        @BeforeAll
        @JvmStatic
        fun initRedis() {
            redis.initRedis()
        }
    }
}