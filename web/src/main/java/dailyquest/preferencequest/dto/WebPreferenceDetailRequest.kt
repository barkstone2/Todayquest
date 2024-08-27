package dailyquest.preferencequest.dto

import dailyquest.quest.entity.DetailQuestType
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.hibernate.validator.constraints.Range

data class WebPreferenceDetailRequest(
    @field:NotBlank(message = "{NotBlank.details.title}")
    @field:Size(max = 50, message = "{Size.details.title}")
    override val title: String = "",
    override val type: DetailQuestType = DetailQuestType.CHECK,
    @field:Range(min = 1, max = 255, message = "{Range.details.targetCount}")
    override val targetCount: Int = 1,
): PreferenceDetailRequest