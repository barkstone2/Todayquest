package dailyquest.achievement.dto

import dailyquest.achievement.entity.AchievementType

data class AchievementAchieveRequest(
    val type: AchievementType,
    val currentValue: Int,
    val userId: Long
) {
}