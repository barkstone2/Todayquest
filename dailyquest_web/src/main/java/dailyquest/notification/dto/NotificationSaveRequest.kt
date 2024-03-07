package dailyquest.notification.dto

import com.fasterxml.jackson.databind.ObjectMapper
import dailyquest.notification.entity.Notification
import dailyquest.notification.entity.NotificationType
import dailyquest.user.entity.UserInfo

interface NotificationSaveRequest {
    val notificationType: NotificationType
    val userId: Long

    fun getNotificationTitle(): String {
        return notificationType.title
    }
    fun createNotificationContent(): String
    fun createNotificationMetadata(): Map<String, String>

    fun mapToEntity(userInfo: UserInfo, objectMapper: ObjectMapper): Notification {
        val metadataJson = objectMapper.writeValueAsString(createNotificationMetadata())
        return Notification(notificationType, userInfo, getNotificationTitle(), createNotificationContent(), metadataJson)
    }
}