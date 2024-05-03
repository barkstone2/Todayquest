package dailyquest.achievement.dto

import dailyquest.achievement.entity.Achievement
import dailyquest.achievement.entity.AchievementAchieveLog
import dailyquest.achievement.entity.AchievementType
import java.time.LocalDateTime

data class AchievementResponse(
    val id: Long = 0,
    val title: String = "",
    val description: String = "",
    val type: AchievementType,
    val targetValue: Long,
    val inactivated: Boolean,
    val isAchieved: Boolean = false,
    val achievedDate: LocalDateTime? = null,
    val targetMessage: String = type.representationFormat.format(targetValue)
) {
    companion object {
        @JvmStatic
        fun from(achievement: Achievement): AchievementResponse {
            return AchievementResponse(achievement.id, achievement.title, achievement.description, achievement.type, achievement.targetValue, achievement.inactivated)
        }

        @JvmStatic
        fun from(achievementAchieveLog: AchievementAchieveLog): AchievementResponse {
            return AchievementResponse(
                achievementAchieveLog.achievement.id,
                achievementAchieveLog.achievement.title,
                achievementAchieveLog.achievement.description,
                achievementAchieveLog.achievement.type,
                achievementAchieveLog.achievement.targetValue,
                achievementAchieveLog.achievement.inactivated,
                true,
                achievementAchieveLog.createdDate
            )
        }
    }
}