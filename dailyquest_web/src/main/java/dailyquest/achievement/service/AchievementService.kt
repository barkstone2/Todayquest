package dailyquest.achievement.service

import dailyquest.achievement.dto.AchievementAchieveRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Transactional
@Service
class AchievementService @Autowired constructor(
    val achievementQueryService: AchievementQueryService,
    val achievementLogService: AchievementLogService,
) {

    fun checkAndAchieveAchievements(achieveRequest: AchievementAchieveRequest) {
        val notAchievedAchievement = achievementQueryService.getNotAchievedAchievement(achieveRequest.type, achieveRequest.userId) ?: return
        if (notAchievedAchievement.canAchieve(achieveRequest.currentValue)) {
            achievementLogService.achieve(notAchievedAchievement, achieveRequest.userId)
        }
    }
}