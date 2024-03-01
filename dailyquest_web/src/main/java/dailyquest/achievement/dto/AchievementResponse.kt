package dailyquest.achievement.dto

import dailyquest.achievement.entity.AchievementType
import java.time.LocalDateTime

data class AchievementResponse(
    val title: String = "",
    val description: String = "",
    val type: AchievementType,
    val targetValue: Int,
    val isAchieved: Boolean = false,
    val achievedDate: LocalDateTime? = null,
) {
}