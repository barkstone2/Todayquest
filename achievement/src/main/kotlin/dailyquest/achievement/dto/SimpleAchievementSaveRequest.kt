package dailyquest.achievement.dto

import dailyquest.achievement.entity.AchievementType

data class SimpleAchievementSaveRequest(
    override val title: String,
    override val description: String,
    override val type: AchievementType,
    override val targetValue: Long
) : AchievementSaveRequest