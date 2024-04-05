package dailyquest.achievement.dto

import dailyquest.achievement.entity.AchievementType

data class AchievementAchieveRequest(
    val type: AchievementType,
    val userId: Long,
    val currentValue: Int
) {

    companion object {
        @JvmStatic
        fun of(type: AchievementType, userId: Long, currentValue: Int): AchievementAchieveRequest {
            return AchievementAchieveRequest(type, userId, currentValue)
        }
    }
}