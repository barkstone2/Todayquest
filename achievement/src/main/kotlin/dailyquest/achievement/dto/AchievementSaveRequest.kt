package dailyquest.achievement.dto

import dailyquest.achievement.entity.Achievement
import dailyquest.achievement.entity.AchievementType

open class AchievementSaveRequest(
    open val title: String,
    open val description: String,
    val type: AchievementType,
    open val targetValue: Long,
) {
    fun mapToEntity(): Achievement {
        return Achievement(title, description, type, targetValue)
    }
}