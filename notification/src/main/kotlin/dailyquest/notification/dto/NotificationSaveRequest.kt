package dailyquest.notification.dto

import dailyquest.notification.entity.Notification
import dailyquest.notification.entity.NotificationType

interface NotificationSaveRequest {
    val notificationType: NotificationType
    val userId: Long

    fun getNotificationTitle(): String {
        return notificationType.title
    }
    fun createNotificationContent(): String
    fun createNotificationMetadata(): Map<String, Any>

    fun mapToEntity(): Notification {
        return Notification.of(notificationType, userId, this.getNotificationTitle(), this.createNotificationContent(), this.createNotificationMetadata())
    }
}