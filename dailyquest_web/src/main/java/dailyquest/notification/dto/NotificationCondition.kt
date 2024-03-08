package dailyquest.notification.dto

import dailyquest.notification.entity.NotificationType
import jakarta.validation.constraints.Min

class NotificationCondition(
    @field:Min(0)
    val page: Int = 0,
    val type: NotificationType? = null,
) {
}