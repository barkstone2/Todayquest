package dailyquest.notification.dto

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dailyquest.notification.entity.Notification
import dailyquest.notification.entity.NotificationType
import java.time.LocalDateTime

class NotificationResponse private constructor(
    val title: String,
    val content: String,
    val type: NotificationType,
    val createdDate: LocalDateTime,
    val metadata: Map<String, String> = mapOf(),
    val confirmedDate: LocalDateTime? = null
) {
    companion object {
        @JvmStatic
        fun from(notification: Notification, objectMapper: ObjectMapper): NotificationResponse {
            val metadataMap: Map<String, String> = objectMapper.readValue(notification.metadata)
            return NotificationResponse(notification.title, notification.content, notification.type, notification.createdDate, metadataMap, notification.confirmedDate)
        }

        @JvmStatic
        fun of(title: String, content: String, type: NotificationType, createdDate: LocalDateTime, metadata: String, objectMapper: ObjectMapper, confirmedDate: LocalDateTime? = null): NotificationResponse {
            val metadataMap: Map<String, String> = objectMapper.readValue(metadata)
            return NotificationResponse(title, content, type, createdDate, metadataMap, confirmedDate)
        }
    }
}