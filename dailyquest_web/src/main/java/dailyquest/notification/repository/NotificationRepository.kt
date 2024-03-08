package dailyquest.notification.repository

import dailyquest.notification.entity.Notification
import org.springframework.data.jpa.repository.JpaRepository

interface NotificationRepository: JpaRepository<Notification, Long>, NotificationRepositoryCustom {
}