package dailyquest.notification.service

import com.fasterxml.jackson.databind.ObjectMapper
import dailyquest.notification.dto.NotificationSaveRequest
import dailyquest.notification.repository.NotificationRepository
import dailyquest.user.repository.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Transactional
@Service
class NotificationCommandService @Autowired constructor(
    val notificationRepository: NotificationRepository,
    val userRepository: UserRepository,
    val objectMapper: ObjectMapper
) {

    fun saveNotification(saveRequest: NotificationSaveRequest, userId: Long) {
        val userReference = userRepository.getReferenceById(userId)
        val saveEntity = saveRequest.mapToEntity(userReference, objectMapper)
        notificationRepository.save(saveEntity)
    }

    fun confirmNotification(notificationId: Long, userId: Long) {
        val notification = notificationRepository.getNotificationByIdAndUserId(notificationId, userId)
        notification?.confirmNotification()
    }

    fun confirmAllNotifications(userId: Long) {
        notificationRepository.confirmAllNotifications(userId)
    }

    fun deleteNotification(notificationId: Long, userId: Long) {
        val notification = notificationRepository.getNotificationByIdAndUserId(notificationId, userId)
        notification?.deleteNotification()
    }

    fun deleteAllNotifications(userId: Long) {
        notificationRepository.deleteAllNotifications(userId)
    }
}