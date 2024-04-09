package dailyquest.user.dto

import dailyquest.user.entity.ProviderType
import dailyquest.user.entity.RoleType
import dailyquest.user.entity.User
import java.time.LocalDate
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
    val questRegistrationCount: Long = 0,
    val questCompletionCount: Long = 0,
    val currentQuestContinuousRegistrationDays: Long = 0,
    val currentQuestContinuousCompletionDays: Long = 0,
    val maxQuestContinuousRegistrationDays: Long = 0,
    val maxQuestContinuousCompletionDays: Long = 0,
    val lastQuestRegistrationDate: LocalDate? = null,
    val lastQuestCompletionDate: LocalDate? = null,
    val perfectDayCount: Long = 0,
    val goldEarnAmount: Long = 0,
    val goldUseAmount: Long = 0,
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
                 user.questRegistrationCount,
                 user.questCompletionCount,
                 user.currentQuestContinuousRegistrationDays,
                 user.currentQuestContinuousCompletionDays,
                 user.maxQuestContinuousRegistrationDays,
                 user.maxQuestContinuousCompletionDays,
                 user.lastQuestRegistrationDate,
                 user.lastQuestCompletionDate,
                 user.perfectDayCount,
                 user.goldEarnAmount,
                 user.goldUseAmount,
            )
        }
    }
}