package dailyquest.achievement.dto

import dailyquest.achievement.entity.AchievementType

data class SimpleAchievementAchieveRequest(
    override val type: AchievementType,
    override val userId: Long,
    override val currentValue: Long
) : AchievementAchieveRequest {
    companion object {
        @JvmStatic
        fun of(type: AchievementType, userId: Long, currentValue: Long): AchievementAchieveRequest {
            return SimpleAchievementAchieveRequest(type, userId, currentValue)
        }
    }
}