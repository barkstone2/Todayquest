package dailyquest.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "batch.context")
class BatchContextProperties(
    val targetAchievementKey: String,
    val achievedLogsKey: String,
    val notifiedUserIdsKey: String,
)