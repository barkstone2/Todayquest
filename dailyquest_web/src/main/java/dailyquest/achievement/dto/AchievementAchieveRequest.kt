package dailyquest.achievement.dto

import dailyquest.achievement.entity.AchievementType

data class AchievementAchieveRequest(
    val type: AchievementType,
    val userId: Long
) {

    companion object {
        @JvmStatic
        fun of(type: AchievementType, userId: Long): AchievementAchieveRequest {
            return AchievementAchieveRequest(type, userId)
        }
    }
}