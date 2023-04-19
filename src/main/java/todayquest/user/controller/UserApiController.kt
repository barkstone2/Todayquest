package todayquest.user.controller

import jakarta.validation.Valid
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import todayquest.common.MessageUtil
import todayquest.exception.DuplicateNicknameException
import todayquest.user.dto.UserPrincipal
import todayquest.user.dto.UserRequestDto
import todayquest.user.service.UserService

@Validated
@RequestMapping("/api/v1/users")
@RestController
class UserApiController(
    private val userService: UserService
) {

    @PatchMapping
    fun updateUser(
        @Valid @RequestBody dto: UserRequestDto,
        @AuthenticationPrincipal principal: UserPrincipal
    ) {
        try {
            userService.changeUserSettings(principal, dto)
        } catch (e: DataIntegrityViolationException) {
            throw DuplicateNicknameException(MessageUtil.getMessage("nickname.duplicate"))
        }
    }

}