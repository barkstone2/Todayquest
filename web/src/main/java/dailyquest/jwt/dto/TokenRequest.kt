package dailyquest.jwt.dto

import dailyquest.user.entity.ProviderType

data class TokenRequest(
    var idToken: String,
    var providerType: ProviderType,
)