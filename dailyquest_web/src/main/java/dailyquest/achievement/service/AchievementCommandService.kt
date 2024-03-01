package dailyquest.achievement.service

import dailyquest.achievement.dto.AchievementAchieveRequest
import dailyquest.achievement.util.AchievementCurrentValueResolver
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Transactional
@Service
class AchievementCommandService @Autowired constructor(
    private val achievementQueryService: AchievementQueryService,
    private val achievementLogCommandService: AchievementLogCommandService,
    private val achievementCurrentValueResolver: AchievementCurrentValueResolver,
) {

    @Async
    fun checkAndAchieveAchievement(achieveRequest: AchievementAchieveRequest) {
        val targetAchievement = achievementQueryService.getNotAchievedAchievement(achieveRequest.type, achieveRequest.userId)
        val currentValue = achievementCurrentValueResolver.resolveCurrentValue(achieveRequest, targetAchievement)
        if (targetAchievement.canAchieve(currentValue)) {
            achievementLogCommandService.achieve(targetAchievement, achieveRequest.userId)
        }
    }
}