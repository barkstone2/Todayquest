package dailyquest.achievement.service

import dailyquest.achievement.dto.AchievementAchieveRequest
import dailyquest.achievement.entity.Achievement
import dailyquest.achievement.entity.AchievementType.*
import dailyquest.quest.service.QuestLogService
import dailyquest.user.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Transactional
@Service
class AchievementCommandService @Autowired constructor(
    val achievementQueryService: AchievementQueryService,
    val achievementLogCommandService: AchievementLogCommandService,
    val questLogService: QuestLogService,
    val userService: UserService,
) {

    fun checkAndAchieveAchievement(achieveRequest: AchievementAchieveRequest) {
        val targetAchievement = achievementQueryService.getNotAchievedAchievement(achieveRequest.type, achieveRequest.userId)
        val currentValue = this.resolveCurrentValue(achieveRequest, targetAchievement)
        if (targetAchievement.canAchieve(currentValue)) {
            achievementLogCommandService.achieve(targetAchievement, achieveRequest.userId)
        }
    }

    private fun resolveCurrentValue(
        achieveRequest: AchievementAchieveRequest,
        targetAchievement: Achievement
    ): Int {
        return when (achieveRequest.type) {
            QUEST_REGISTRATION -> questLogService.getTotalRegistrationCount(achieveRequest.userId)
            QUEST_COMPLETION -> questLogService.getTotalCompletionCount(achieveRequest.userId)
            QUEST_CONTINUOUS_REGISTRATION_DAYS -> questLogService.getRegDaysFrom(achieveRequest.userId, targetAchievement.targetValue)
            EMPTY, USER_LEVEL, USER_GOLD_EARN -> 0
        }
    }
}