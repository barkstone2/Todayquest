package dailyquest.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "notification.page")
class NotificationPageSizeProperties(
    val size: Int = 10
)