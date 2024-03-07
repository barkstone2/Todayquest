package dailyquest.notification.service

import com.fasterxml.jackson.databind.ObjectMapper
import dailyquest.notification.dto.NotificationSaveRequest
import dailyquest.notification.repository.NotificationRepository
import dailyquest.user.repository.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Async
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
}