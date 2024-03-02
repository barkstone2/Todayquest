package dailyquest.context

import dailyquest.common.CustomElasticsearchContainer
import dailyquest.common.CustomRedisContainer
import dailyquest.jwt.JwtTokenProvider
import dailyquest.user.repository.UserRepository
import org.junit.jupiter.api.BeforeAll
import org.springframework.web.context.WebApplicationContext
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
open class IntegrationTestContextWithRedisAndElasticsearch(
    context: WebApplicationContext,
    userRepository: UserRepository,
    jwtTokenProvider: JwtTokenProvider,
) : IntegrationTestContext(context, userRepository, jwtTokenProvider) {
    companion object {
        @JvmStatic
        @Container
        val elasticsearch = CustomElasticsearchContainer()

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