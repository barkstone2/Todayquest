package dailyquest.user.dto

import dailyquest.user.entity.ProviderType
import dailyquest.user.entity.UserInfo

class UserSaveRequest(
    val oauth2Id: String,
    val nickname: String,
    val providerType: ProviderType,
) {
    fun mapToEntity(): UserInfo {
        return UserInfo(oauth2Id, nickname, providerType)
    }
}