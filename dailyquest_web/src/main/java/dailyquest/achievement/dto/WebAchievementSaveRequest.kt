package dailyquest.achievement.dto

import dailyquest.achievement.entity.AchievementType
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

class WebAchievementSaveRequest(
    @field:NotBlank(message = "{achievement.title.notBlank}")
    @field:Size(max = 50, message = "{achievement.title.size}")
    override val title: String,
    @field:NotBlank(message = "{achievement.description.notBlank}")
    @field:Size(max = 150, message = "{achievement.description.size}")
    override val description: String,
    type: AchievementType,
    @field:Min(value = 1, message = "{achievement.targetValue.min}")
    override val targetValue: Long
) : AchievementSaveRequest(title, description, type, targetValue)