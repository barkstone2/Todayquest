package dailyquest.user.dto

import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

class WebUserUpdateRequest(
    @field:Pattern(
        regexp = "^([a-zA-Z0-9가-힣[^\\sㄱ-ㅎㅏ-ㅡ]]+)([a-zA-Z0-9가-힣\\s]*)([a-zA-Z0-9가-힣[^\\sㄱ-ㅎㅏ-ㅡ]]+)$",
        message = "{nickname.pattern}"
    )
    @field:Size(max = 20, message = "{nickname.size}")
    override val nickname: String?,
    coreTime: Int?,
): UserUpdateRequest(nickname, coreTime) {
}