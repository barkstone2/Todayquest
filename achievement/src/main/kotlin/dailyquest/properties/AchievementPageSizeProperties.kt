package dailyquest.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "achievement.page")
class AchievementPageSizeProperties(
    val size: Int = 10
)