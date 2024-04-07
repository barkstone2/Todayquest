package dailyquest.common

import dailyquest.properties.BatchApiProperties
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class BatchApiUtil(
    private val batchRestTemplate: RestTemplate,
    private val batchApiProperties: BatchApiProperties
) {
    fun checkAndAchieve(achievementId: Long) {
        batchRestTemplate.postForEntity(batchApiProperties.getCheckAndAchieveUrl(), achievementId, Void::class.java)
    }
}