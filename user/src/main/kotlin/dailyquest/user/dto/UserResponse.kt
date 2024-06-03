package dailyquest.user.dto

import dailyquest.user.entity.ProviderType
import dailyquest.user.entity.RoleType
import dailyquest.user.entity.User
import java.time.LocalDateTime
import java.time.LocalTime

class UserResponse @JvmOverloads constructor(
    val id: Long,
    val nickname: String,
    val providerType: ProviderType = ProviderType.GOOGLE,
    val coreTime: LocalTime = LocalTime.of(8, 0),
    val coreTimeLastModifiedDate: LocalDateTime? = null,
    val exp: Long = 0,
    val gold: Long = 0,
    val role: RoleType = RoleType.USER,
) {
    fun calculateLevel(expTable: Map<Int, Long>): Triple<Int, Long, Long> {
        var level = 1
        var remainingExp = exp
        var requiredExp = 0L

        expTable.keys.sorted().forEach { key ->
            requiredExp = expTable[key] ?: return@forEach

            if (requiredExp == 0L) return@forEach

            if (remainingExp >= requiredExp) {
                remainingExp -= requiredExp
                level++
            } else {
                return Triple(level, remainingExp, requiredExp)
            }
        }

        return Triple(level, remainingExp, requiredExp)
    }

    companion object {
        @JvmStatic
        fun from(user: User): UserResponse {
            return UserResponse(
                 user.id,
                 user.nickname,
                 user.providerType,
                 user.coreTime,
                 user.coreTimeLastModifiedDate,
                 user.exp,
                 user.gold,
                 user.role,
            )
        }
    }
}