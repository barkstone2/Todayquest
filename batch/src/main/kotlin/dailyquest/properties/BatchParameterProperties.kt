package dailyquest.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "batch.param")
class BatchParameterProperties(
    val targetAchievementIdKey: String
) {
}