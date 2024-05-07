package dailyquest.notification.controller

import dailyquest.common.ResponseData
import dailyquest.common.RestPage
import dailyquest.notification.dto.NotificationCondition
import dailyquest.notification.dto.NotificationResponse
import dailyquest.notification.service.NotificationService
import dailyquest.user.dto.UserPrincipal
import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@Validated
@RequestMapping("/api/v1/notifications")
@RestController
class NotificationApiController @Autowired constructor(
    private val notificationService: NotificationService,
) {

    @GetMapping("/not-confirmed")
    fun getNotConfirmedNotifications(
        @AuthenticationPrincipal principal: UserPrincipal,
        @Valid notificationCondition: NotificationCondition
    ): ResponseEntity<ResponseData<RestPage<NotificationResponse>>> {
        val notConfirmedNotificationsOfUser =
            notificationService.getNotConfirmedNotificationsOfUser(principal.id, notificationCondition)
        return ResponseEntity.ok(ResponseData.of(RestPage(notConfirmedNotificationsOfUser)))
    }

    @GetMapping("")
    fun getActiveNotifications(
        @AuthenticationPrincipal principal: UserPrincipal,
        @Valid notificationCondition: NotificationCondition
    ): ResponseEntity<ResponseData<RestPage<NotificationResponse>>> {
        val activeNotificationsOfUser =
            notificationService.getActiveNotificationsOfUser(principal.id, notificationCondition)
        return ResponseEntity.ok(ResponseData.of(RestPage(activeNotificationsOfUser)))
    }

    @PatchMapping("/{notificationId}/confirm")
    fun confirmNotification(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable notificationId: Long,
    ) {
        notificationService.confirmNotification(notificationId, principal.id)
    }

    @PatchMapping("/confirm-all")
    fun confirmAllNotifications(
        @AuthenticationPrincipal principal: UserPrincipal,
    ) {
        notificationService.confirmAllNotifications(principal.id)
    }

    @PatchMapping("/{notificationId}/delete")
    fun deleteNotification(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable notificationId: Long,
    ) {
        notificationService.deleteNotification(notificationId, principal.id)
    }

    @PatchMapping("/delete-all")
    fun deleteAllNotifications(
        @AuthenticationPrincipal principal: UserPrincipal,
    ) {
        notificationService.deleteAllNotifications(principal.id)
    }
}