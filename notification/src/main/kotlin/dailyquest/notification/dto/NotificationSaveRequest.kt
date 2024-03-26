package dailyquest.notification.dto

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import dailyquest.notification.entity.Notification
import dailyquest.notification.entity.NotificationType

interface NotificationSaveRequest {
    val notificationType: NotificationType
    val userId: Long

    fun getNotificationTitle(): String {
        return notificationType.title
    }
    fun createNotificationContent(): String
    fun createNotificationMetadata(): Map<String, String>
    fun createNotificationMetadataJson(): String

    fun mapToEntity(): Notification {
        val metadataJson = createNotificationMetadataJson()
        return Notification(notificationType, userId, getNotificationTitle(), createNotificationContent(), metadataJson)
    }
}