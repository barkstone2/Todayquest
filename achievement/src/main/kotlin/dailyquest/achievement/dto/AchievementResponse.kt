package dailyquest.achievement.dto

import dailyquest.achievement.entity.Achievement
import dailyquest.achievement.entity.AchievementType
import java.time.LocalDateTime

data class AchievementResponse(
    val id: Long = 0,
    val title: String = "",
    val description: String = "",
    val type: AchievementType,
    val targetValue: Int,
    val isAchieved: Boolean = false,
    val achievedDate: LocalDateTime? = null,
) {
    companion object {
        @JvmStatic
        fun from(achievement: Achievement): AchievementResponse {
            return AchievementResponse(achievement.id, achievement.title, achievement.description, achievement.type, achievement.targetValue)
        }
    }
}