package dailyquest.achievement.dto

import dailyquest.achievement.entity.AchievementType

data class AdminAchievementResponse(
    val achievementTypes: List<AchievementType>,
    val achievements: Map<AchievementType, List<AchievementResponse>>
)