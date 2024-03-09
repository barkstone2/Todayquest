package dailyquest.notification.service

import dailyquest.common.RestPage
import dailyquest.notification.dto.NotificationCondition
import dailyquest.notification.dto.NotificationResponse
import dailyquest.notification.dto.NotificationSaveRequest
import dailyquest.sse.SseService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service

@Service
class NotificationService @Autowired constructor(
    private val notificationCommandService: NotificationCommandService,
    private val notificationQueryService: NotificationQueryService,
    private val sseService: SseService
) {
    fun getNotConfirmedNotificationsOfUser(userId: Long, condition: NotificationCondition): RestPage<NotificationResponse> {
        return notificationQueryService.getNotConfirmedNotificationsOfUser(userId, condition)
    }

    fun getActiveNotificationsOfUser(userId: Long, condition: NotificationCondition): RestPage<NotificationResponse> {
        return notificationQueryService.getActiveNotificationsOfUser(userId, condition)
    }

    @Async
    fun saveNotification(saveRequest: NotificationSaveRequest, userId: Long) {
        notificationCommandService.saveNotification(saveRequest, userId)
        sseService.sendNotificationEvent(userId)
    }

    @Async
    fun confirmNotification(notificationId: Long, userId: Long) {
        notificationCommandService.confirmNotification(notificationId, userId)
        sseService.sendNotificationEvent(userId)
    }

    @Async
    fun confirmAllNotifications(userId: Long) {
        notificationCommandService.confirmAllNotifications(userId)
        sseService.sendNotificationEvent(userId)
    }

    @Async
    fun deleteNotification(notificationId: Long, userId: Long) {
        notificationCommandService.deleteNotification(notificationId, userId)
        sseService.sendNotificationEvent(userId)
    }

    @Async
    fun deleteAllNotifications(userId: Long) {
        notificationCommandService.deleteAllNotifications(userId)
        sseService.sendNotificationEvent(userId)
    }
}