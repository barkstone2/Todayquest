package dailyquest.context

import dailyquest.common.CustomRedisContainer
import org.junit.jupiter.api.BeforeAll
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
class ElasticsearchTestContainerContext {
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