package dailyquest.achievement.util

import dailyquest.achievement.dto.AchievementAchieveRequest
import dailyquest.achievement.entity.Achievement

interface AchievementCurrentValueResolver {
    fun resolveCurrentValue(
        achieveRequest: AchievementAchieveRequest,
        targetAchievement: Achievement
    ): Int
}