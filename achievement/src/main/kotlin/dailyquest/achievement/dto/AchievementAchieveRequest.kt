package dailyquest.achievement.dto

import dailyquest.achievement.entity.AchievementType

interface AchievementAchieveRequest {
    val type: AchievementType
    val userId: Long
    val currentValue: Long
}