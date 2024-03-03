package dailyquest.notification.entity

import dailyquest.common.CreatedTimeEntity
import dailyquest.user.entity.UserInfo
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
class Notification(
    type: NotificationType,
    user: UserInfo,
    title: String,
    content: String = "",
    metadata: String = "",
): CreatedTimeEntity() {

    @Column(name = "notification_id")
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0

    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    val user: UserInfo = user

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

    @Column(name = "deleted_date", insertable = false)
    var deletedDate: LocalDateTime? = null

    fun confirmNotification() {
        this.confirmedDate = LocalDateTime.now()
    }

    fun deleteNotification() {
        this.deletedDate = LocalDateTime.now()
    }

    companion object {
        @JvmStatic
        fun of(
            type: NotificationType,
            user: UserInfo,
            title: String,
            content: String = "",
            metadata: String = "",
        ): Notification {
            return Notification(type, user, title, content, metadata)
        }
    }

}