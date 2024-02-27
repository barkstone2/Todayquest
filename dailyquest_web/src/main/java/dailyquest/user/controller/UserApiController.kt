package dailyquest.user.controller

import jakarta.validation.Valid
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import dailyquest.common.MessageUtil
import dailyquest.common.ResponseData
import dailyquest.exception.DuplicateNicknameException
import dailyquest.user.dto.UserPrincipal
import dailyquest.user.dto.UserUpdateRequest
import dailyquest.user.service.UserService

@Validated
@RequestMapping("/api/v1/users")
@RestController
class UserApiController(
    private val userService: UserService
) {

    @GetMapping
    fun getPrincipal(@AuthenticationPrincipal principal: UserPrincipal): ResponseEntity<ResponseData<UserPrincipal>> {
        return ResponseEntity.ok(ResponseData(principal))
    }

    @PatchMapping
    fun updateUser(
        @Valid @RequestBody dto: UserUpdateRequest,
        @AuthenticationPrincipal principal: UserPrincipal
    ) {
        try {
            userService.updateUser(principal, dto)
        } catch (e: DataIntegrityViolationException) {
            throw DuplicateNicknameException(MessageUtil.getMessage("nickname.duplicate"))
        }
    }

}