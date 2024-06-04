package dailyquest.context

import dailyquest.common.CustomOpenSearchContainer
import dailyquest.common.CustomRedisContainer
import org.junit.jupiter.api.BeforeAll
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
open class IntegrationTestContextWithRedisAndElasticsearch : IntegrationTestContext() {
    companion object {
        @JvmStatic
        @Container
        val elasticsearch = CustomOpenSearchContainer()

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