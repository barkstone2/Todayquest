package dailyquest.config

import dailyquest.properties.InternalApiProperties
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate

@Configuration
class RestTemplateConfig(
    private val internalApiProperties: InternalApiProperties,
    private val restTemplateBuilder: RestTemplateBuilder
) {
    @Bean
    fun webRestTemplate(): RestTemplate {
        val internalApiTemplateBuilder =
            restTemplateBuilder.defaultHeader(internalApiProperties.keyHeaderName, internalApiProperties.key)
        return internalApiTemplateBuilder.build()
    }
}