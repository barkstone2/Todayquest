package dailyquest.achievement.dto

import dailyquest.achievement.entity.AchievementType

class AchievementRequest(
    val type: AchievementType,
    val currentValue: Int,
) {
}