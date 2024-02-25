package dailyquest.achievement.dto

import dailyquest.achievement.entity.AchievementType

class AchievementAchieveRequest(
    val type: AchievementType,
    val currentValue: Int,
    val userId: Long
) {
}