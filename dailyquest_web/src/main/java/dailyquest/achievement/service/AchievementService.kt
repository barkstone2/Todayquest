package dailyquest.achievement.service

import dailyquest.achievement.entity.Achievement
import dailyquest.achievement.entity.AchievementType
import dailyquest.log.gold.earn.service.GoldEarnLogService
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
    val userService: UserService,
    val userGoldEarnLogService: GoldEarnLogService
) {

    fun checkAndAchieveAchievements(achievementType: AchievementType, userId: Long) {
        val notAchievedAchievements = achievementQueryService.getNotAchievedAchievements(achievementType, userId)
        val achievableAchievements = mutableListOf<Achievement>()
        for (targetAchievement in notAchievedAchievements) {
            val currentValue = this.resolveCurrentValue(targetAchievement, userId)
            if (targetAchievement.canAchieve(currentValue)) {
                achievableAchievements.add(targetAchievement)
            } else {
                break
            }
        }

        achievementLogService.achieveAll(achievableAchievements, userId)
    }

    private fun resolveCurrentValue(targetAchievement: Achievement, userId: Long): Int {
        return when (targetAchievement.type) {
            AchievementType.QUEST_TOTAL_REGISTRATION -> questLogService.getTotalRegistrationCount(userId)
            AchievementType.QUEST_TOTAL_COMPLETION -> questLogService.getTotalCompletionCount(userId)
            AchievementType.QUEST_CONTINUOUS_REGISTRATION_DAYS -> questLogService.getContinuousRegistrationCount(userId, targetAchievement.targetValue)
            AchievementType.USER_LEVEL -> userService.getUserById(userId).level
            AchievementType.USER_GOLD_EARN -> userGoldEarnLogService.getTotalGoldEarnOfUser(userId)
        }
    }
}