package dailyquest.achievement.service

import dailyquest.achievement.entity.Achievement
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
        val notAchievedAchievements = achievementQueryService.getNotAchievedAchievements(achievementType, userId)
        val achievableAchievements = mutableListOf<Achievement>()
        for (targetAchievement in notAchievedAchievements) {
            if (this.canAchieve(targetAchievement, userId)) {
                achievableAchievements.add(targetAchievement)
            } else {
                break
            }
        }

        achievementLogService.achieveAll(achievableAchievements, userId)
    }

    private fun canAchieve(targetAchievement: Achievement, userId: Long): Boolean {
        val targetValue = targetAchievement.targetValue
        val currentValue = when (targetAchievement.type) {
            AchievementType.QUEST_TOTAL_REGISTRATION -> questLogService.getTotalRegistrationCount(userId)
            AchievementType.QUEST_TOTAL_COMPLETION -> questLogService.getTotalCompletionCount(userId)
            AchievementType.QUEST_CONTINUOUS_REGISTRATION_DAYS -> questLogService.getContinuousRegistrationCount(userId, targetValue)
            AchievementType.USER_LEVEL -> userService.getUserById(userId).level
        }
        return currentValue >= targetValue
    }
}