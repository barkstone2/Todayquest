package dailyquest.user.controller

import dailyquest.common.ResponseData
import dailyquest.exception.DuplicateNicknameException
import dailyquest.notification.service.NotificationService
import dailyquest.user.dto.UserPrincipal
import dailyquest.user.dto.WebUserUpdateRequest
import dailyquest.user.service.UserService
import jakarta.validation.Valid
import org.springframework.context.support.MessageSourceAccessor
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@Validated
@RequestMapping("/api/v1/users")
@RestController
class UserApiController(
    private val userService: UserService,
    private val notificationService: NotificationService,
    private val messageSourceAccessor: MessageSourceAccessor
) {
    @GetMapping
    fun getPrincipal(@AuthenticationPrincipal principal: UserPrincipal): ResponseEntity<ResponseData<UserPrincipal>> {
        principal.notificationCount = notificationService.getNotConfirmedNotificationCount(principal.id)
        return ResponseEntity.ok(ResponseData(principal))
    }

    @PatchMapping
    fun updateUser(
        @Valid @RequestBody updateRequest: WebUserUpdateRequest,
        @AuthenticationPrincipal principal: UserPrincipal
    ) {
        try {
            userService.updateUser(principal.id, updateRequest)
        } catch (e: DataIntegrityViolationException) {
            throw DuplicateNicknameException(messageSourceAccessor.getMessage("nickname.duplicate"))
        }
    }
}