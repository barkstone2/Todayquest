package dailyquest.notification.dto

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import dailyquest.notification.entity.Notification
import dailyquest.notification.entity.NotificationType
import java.time.LocalDateTime

class NotificationResponse private constructor(
    val id: Long,
    val title: String,
    val content: String,
    val type: NotificationType,
    val createdDate: LocalDateTime,
    val metadata: Map<String, String> = mapOf(),
    val confirmedDate: LocalDateTime? = null
) {
    companion object {
        @JvmStatic
        private val objectMapper = jacksonObjectMapper().registerKotlinModule().registerModule(JavaTimeModule())

        @JvmStatic
        fun from(notification: Notification): NotificationResponse {
            val metadataMap: Map<String, String> = objectMapper.readValue(notification.metadata)
            return NotificationResponse(notification.id, notification.title, notification.content, notification.type, notification.createdDate, metadataMap, notification.confirmedDate)
        }
    }
}