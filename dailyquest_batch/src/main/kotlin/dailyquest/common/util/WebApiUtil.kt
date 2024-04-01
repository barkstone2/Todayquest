package dailyquest.common.util

import dailyquest.properties.InternalApiProperties
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class WebApiUtil(
    private val webRestTemplate: RestTemplate,
    private val internalApiProperties: InternalApiProperties
) {
    fun postSseNotify(userIds: List<Long>) {
        webRestTemplate.postForEntity(internalApiProperties.getSseNotifyUrl(), userIds, Void::class.java)
    }
}