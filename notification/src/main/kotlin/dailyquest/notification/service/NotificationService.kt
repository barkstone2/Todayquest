package dailyquest.notification.service

import dailyquest.notification.dto.NotificationCondition
import dailyquest.notification.dto.NotificationResponse
import dailyquest.notification.dto.NotificationSaveRequest
import dailyquest.notification.entity.Notification
import dailyquest.notification.repository.NotificationRepository
import dailyquest.properties.NotificationPageSizeProperties
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Transactional(readOnly = true)
@Service
class NotificationService @Autowired constructor(
    private val notificationRepository: NotificationRepository,
    private val pageSizeProperties: NotificationPageSizeProperties
) {
    fun getNotConfirmedNotificationsOfUser(userId: Long, condition: NotificationCondition): Page<NotificationResponse> {
        val pageRequest = this.createPageRequest(condition)
        val notConfirmedNotifications = notificationRepository.getNotConfirmedNotifications(userId, condition, pageRequest)
        val notificationResponses = this.mapToResponses(notConfirmedNotifications)
        return notificationResponses
    }

    private fun createPageRequest(condition: NotificationCondition): Pageable {
        return PageRequest.of(condition.page, pageSizeProperties.size)
    }

    private fun mapToResponses(notifications: Page<Notification>): Page<NotificationResponse> {
        return notifications.map { NotificationResponse.from(it) }
    }

    fun getActiveNotificationsOfUser(userId: Long, condition: NotificationCondition): Page<NotificationResponse> {
        val pageRequest = this.createPageRequest(condition)
        val activeNotifications = notificationRepository.getActiveNotifications(userId, condition, pageRequest)
        val notificationResponses = this.mapToResponses(activeNotifications)
        return notificationResponses
    }

    @Transactional
    fun saveNotification(saveRequest: NotificationSaveRequest, userId: Long) {
        val saveEntity = saveRequest.mapToEntity()
        notificationRepository.save(saveEntity)
    }

    @Transactional
    fun confirmNotification(notificationId: Long, userId: Long) {
        val notification = notificationRepository.getNotificationByIdAndUserId(notificationId, userId)
        notification?.confirmNotification()
    }

    @Transactional
    fun confirmAllNotifications(userId: Long) {
        notificationRepository.confirmAllNotifications(userId)
    }

    @Transactional
    fun deleteNotification(notificationId: Long, userId: Long) {
        val notification = notificationRepository.getNotificationByIdAndUserId(notificationId, userId)
        notification?.deleteNotification()
    }

    @Transactional
    fun deleteAllNotifications(userId: Long) {
        notificationRepository.deleteAllNotifications(userId)
    }
}