package dailyquest.config

import dailyquest.properties.BatchApiProperties
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate

@Configuration
class RestTemplateConfig(
    private val batchApiProperties: BatchApiProperties,
    private val restTemplateBuilder: RestTemplateBuilder
) {
    @Bean
    fun batchRestTemplate(): RestTemplate {
        return restTemplateBuilder.defaultHeader(batchApiProperties.keyHeaderName, batchApiProperties.key).build()
    }
}