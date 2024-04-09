package dailyquest.achievement.dto

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import dailyquest.achievement.entity.Achievement
import dailyquest.achievement.entity.AchievementType
import java.time.LocalDateTime

data class AchievementResponse @JvmOverloads constructor(
    val id: Long = 0,
    val title: String = "",
    val description: String = "",
    val type: AchievementType,
    val targetValue: Long,
    val isAchieved: Boolean = false,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    val achievedDate: LocalDateTime? = null,
) {
    @JsonProperty
    private val targetMessage = type.representationFormat.format(targetValue)
    companion object {
        @JvmStatic
        fun from(achievement: Achievement): AchievementResponse {
            return AchievementResponse(achievement.id, achievement.title, achievement.description, achievement.type, achievement.targetValue)
        }
    }
}