package dailyquest.achievement.dto

import com.fasterxml.jackson.annotation.JsonFormat
import dailyquest.achievement.entity.Achievement
import dailyquest.achievement.entity.AchievementType

open class AchievementSaveRequest(
    open val title: String,
    open val description: String,
    @JsonFormat(shape = JsonFormat.Shape.STRING) val type: AchievementType,
    open val targetValue: Long,
) {
    fun mapToEntity(): Achievement {
        return Achievement(title, description, type, targetValue)
    }
}