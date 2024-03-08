package dailyquest.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "notification.page")
class NotificationPageSizeProperties {
    var size = 0
}