package dailyquest.notification.service

import com.fasterxml.jackson.databind.ObjectMapper
import dailyquest.common.RestPage
import dailyquest.notification.dto.NotificationCondition
import dailyquest.notification.dto.NotificationResponse
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
class NotificationQueryService @Autowired constructor(
    private val notificationRepository: NotificationRepository,
    private val objectMapper: ObjectMapper,
    private val pageSizeProperties: NotificationPageSizeProperties
) {

    fun getNotConfirmedNotificationsOfUser(userId: Long, condition: NotificationCondition): RestPage<NotificationResponse> {
        val pageRequest = this.createPageRequest(condition)
        val notConfirmedNotifications = notificationRepository.getNotConfirmedNotifications(userId, condition, pageRequest)
        val notificationResponses = this.mapToResponses(notConfirmedNotifications)
        return RestPage(notificationResponses)
    }

    fun getActiveNotificationsOfUser(userId: Long, condition: NotificationCondition): RestPage<NotificationResponse> {
        val pageRequest = this.createPageRequest(condition)
        val activeNotifications = notificationRepository.getActiveNotifications(userId, condition, pageRequest)
        val notificationResponses = this.mapToResponses(activeNotifications)
        return RestPage(notificationResponses)
    }

    private fun createPageRequest(condition: NotificationCondition): Pageable {
        return PageRequest.of(condition.page, pageSizeProperties.size)
    }

    private fun mapToResponses(notifications: Page<Notification>): Page<NotificationResponse> {
        return notifications.map { NotificationResponse.from(it, objectMapper) }
    }
}