package dailyquest.notification.repository

import dailyquest.notification.dto.NotificationCondition
import dailyquest.notification.entity.Notification
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface NotificationRepositoryCustom {
    fun getNotConfirmedNotifications(userId: Long, condition: NotificationCondition, pageable: Pageable): Page<Notification>
    fun getActiveNotifications(userId: Long, condition: NotificationCondition, pageable: Pageable): Page<Notification>
}