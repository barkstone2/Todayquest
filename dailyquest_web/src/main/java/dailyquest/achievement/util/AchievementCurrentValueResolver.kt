package dailyquest.achievement.util

import dailyquest.achievement.dto.AchievementAchieveRequest
import dailyquest.achievement.entity.Achievement
import dailyquest.achievement.entity.AchievementType
import dailyquest.quest.service.QuestLogService
import dailyquest.user.service.UserService
import org.springframework.stereotype.Component

@Component
class AchievementCurrentValueResolver(
    private val questLogService: QuestLogService,
    private val userService: UserService,
) {
    fun resolveCurrentValue(
        achieveRequest: AchievementAchieveRequest,
        targetAchievement: Achievement
    ): Int {
        return when (achieveRequest.type) {
            AchievementType.QUEST_REGISTRATION -> questLogService.getTotalRegistrationCount(achieveRequest.userId)
            AchievementType.QUEST_COMPLETION -> questLogService.getTotalCompletionCount(achieveRequest.userId)
            AchievementType.QUEST_CONTINUOUS_REGISTRATION_DAYS -> questLogService.getRegistrationDaysSince(achieveRequest.userId, targetAchievement.targetValue)
            AchievementType.QUEST_CONTINUOUS_COMPLETION -> questLogService.getCompletionDaysSince(achieveRequest.userId, targetAchievement.targetValue)
            AchievementType.USER_LEVEL -> userService.getUserPrincipal(achieveRequest.userId).level
            AchievementType.EMPTY, AchievementType.USER_GOLD_EARN -> 0
        }
    }
}