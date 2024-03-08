package dailyquest.notification.controller

import dailyquest.common.ResponseData
import dailyquest.common.RestPage
import dailyquest.notification.dto.NotificationCondition
import dailyquest.notification.dto.NotificationResponse
import dailyquest.notification.service.NotificationQueryService
import dailyquest.user.dto.UserPrincipal
import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Validated
@RequestMapping("/api/v1/notifications")
@RestController
class NotificationApiController @Autowired constructor(
    private val notificationQueryService: NotificationQueryService
) {

    @GetMapping("/not-confirmed")
    fun getNotConfirmedNotifications(
        @AuthenticationPrincipal principal: UserPrincipal,
        @Valid notificationCondition: NotificationCondition
    ): ResponseEntity<ResponseData<RestPage<NotificationResponse>>> {
        val notConfirmedNotificationsOfUser =
            notificationQueryService.getNotConfirmedNotificationsOfUser(principal.id, notificationCondition)
        return ResponseEntity.ok(ResponseData.of(notConfirmedNotificationsOfUser))
    }

    @GetMapping("")
    fun getActiveNotifications(
        @AuthenticationPrincipal principal: UserPrincipal,
        @Valid notificationCondition: NotificationCondition
    ): ResponseEntity<ResponseData<RestPage<NotificationResponse>>> {
        val activeNotificationsOfUser =
            notificationQueryService.getActiveNotificationsOfUser(principal.id, notificationCondition)
        return ResponseEntity.ok(ResponseData.of(activeNotificationsOfUser))
    }

}