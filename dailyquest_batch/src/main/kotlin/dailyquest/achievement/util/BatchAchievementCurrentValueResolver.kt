package dailyquest.achievement.util

import dailyquest.achievement.dto.AchievementAchieveRequest
import dailyquest.achievement.entity.Achievement
import org.springframework.stereotype.Component

@Component
class BatchAchievementCurrentValueResolver: AchievementCurrentValueResolver {
    override fun resolveCurrentValue(achieveRequest: AchievementAchieveRequest, targetAchievement: Achievement): Int {
        return 0
    }
}