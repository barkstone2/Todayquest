package dailyquest.context

import dailyquest.common.CustomOpenSearchContainer
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.context.annotation.Import
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
@ExtendWith(MockKExtension::class)
@Import(MockRedisTestContextConfig::class)
open class IntegrationTestContextWithElasticsearch : IntegrationTestContext() {
    companion object {
        @JvmStatic
        @Container
        val elasticsearch = CustomOpenSearchContainer()
    }
}