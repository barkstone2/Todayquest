package dailyquest.achievement.dto

import dailyquest.achievement.entity.Achievement
import dailyquest.achievement.entity.AchievementType
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotEmpty

data class AchievementRequest(
    @field:NotEmpty
    val title: String,
    @field:NotEmpty
    val description: String,
    val type: AchievementType,
    @field:Min(1)
    val targetValue: Int,
) {
    fun mapToEntity(): Achievement {
        return Achievement(title, description, type, targetValue)
    }
}