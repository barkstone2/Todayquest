package dailyquest.common

import dailyquest.properties.BatchApiProperties
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class BatchApiUtil(
    private val batchRestTemplate: RestTemplate,
    private val batchApiProperties: BatchApiProperties
) {
    private val logger: Logger = LoggerFactory.getLogger("batchErrorLogger")

    @Async
    fun checkAndAchieve(achievementId: Long) {
        try {
            batchRestTemplate.postForEntity(batchApiProperties.getCheckAndAchieveUrl(), achievementId, Void::class.java)
        } catch (ex: Exception) {
            logger.error("checkAndAchieve request for $achievementId is failed.")
        }
    }
}