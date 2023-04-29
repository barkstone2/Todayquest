package todayquest.jwt.dto

import todayquest.user.entity.ProviderType

data class TokenRequest(
    var idToken: String,
    var providerType: ProviderType,
)