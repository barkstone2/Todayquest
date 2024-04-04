package dailyquest.user.dto

import dailyquest.user.entity.ProviderType
import dailyquest.user.entity.User

class UserSaveRequest(
    val oauth2Id: String,
    val nickname: String,
    val providerType: ProviderType,
) {
    fun mapToEntity(): User {
        return User(oauth2Id, nickname, providerType)
    }
}