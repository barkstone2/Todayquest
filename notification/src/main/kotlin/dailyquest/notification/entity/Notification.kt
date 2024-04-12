package dailyquest.notification.entity

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import dailyquest.common.CreatedTimeEntity
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
class Notification private constructor(
    type: NotificationType,
    userId: Long,
    title: String,
    content: String,
    metadata: String,
): CreatedTimeEntity() {

    @Column(name = "notification_id")
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0

    @Column(name = "user_id", nullable = false, updatable = false)
    val userId: Long = userId

    @Column(length = 300, nullable = false, updatable = false)
    val title: String = title

    @Column(length = 1000, nullable = false, updatable = false)
    val content: String = content

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    val type: NotificationType = type

    @Column(nullable = false, updatable = false)
    val metadata: String = metadata

    @Column(name = "confirmed_date", insertable = false)
    var confirmedDate: LocalDateTime? = null
        protected set

    @Column(name = "deleted_date", insertable = false)
    var deletedDate: LocalDateTime? = null
        protected set

    fun confirmNotification() {
        if (this.confirmedDate == null && this.deletedDate == null) {
            this.confirmedDate = LocalDateTime.now()
        }
    }

    fun deleteNotification() {
        if (this.deletedDate == null) {
            this.deletedDate = LocalDateTime.now()
        }
    }

    companion object {
        private val om = jacksonObjectMapper().registerModules(JavaTimeModule()).registerKotlinModule()

        @JvmStatic
        fun of(
            type: NotificationType,
            userId: Long,
            title: String,
            content: String = "",
            metadata: Map<String, Any> = mapOf(),
        ): Notification {
            return Notification(type, userId, title, content, om.writeValueAsString(metadata))
        }
    }
}