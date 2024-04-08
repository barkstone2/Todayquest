package dailyquest.achievement.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

class WebAchievementUpdateRequest(
    @field:NotBlank(message = "{achievement.title.notBlank}")
    @field:Size(max = 50, message = "{achievement.title.size}")
    override val title: String,
    @field:NotBlank(message = "{achievement.description.notBlank}")
    @field:Size(max = 150, message = "{achievement.description.size}")
    override val description: String,
) : AchievementUpdateRequest(title, description) {
}