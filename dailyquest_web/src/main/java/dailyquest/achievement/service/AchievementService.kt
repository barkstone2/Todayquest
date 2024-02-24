package dailyquest.achievement.service

import dailyquest.achievement.dto.AchievementRequest
import dailyquest.achievement.entity.AchievementType
import dailyquest.quest.service.QuestLogService
import dailyquest.user.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Transactional
@Service
class AchievementService @Autowired constructor(
    val achievementQueryService: AchievementQueryService,
    val questLogService: QuestLogService,
    val achievementLogService: AchievementLogService,
    val userService: UserService
) {

    fun checkAndAchieveAchievements(achievementType: AchievementType, userId: Long) {
        val currentValue = this.getCurrentValue(achievementType, userId)
        val achievementRequest = AchievementRequest(achievementType, currentValue)
        val achievableAchievements = achievementQueryService.getAchievableAchievements(achievementRequest)
        achievementLogService.achieveAll(achievableAchievements, userId)
    }

    private fun getCurrentValue(type: AchievementType, userId: Long): Int {
        return when (type) {
            AchievementType.QUEST_TOTAL_REGISTRATION -> questLogService.getTotalRegistrationCount(userId)
            AchievementType.QUEST_TOTAL_COMPLETION -> questLogService.getTotalCompletionCount(userId)
        }
    }
}