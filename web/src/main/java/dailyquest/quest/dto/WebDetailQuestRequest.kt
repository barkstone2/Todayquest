package dailyquest.quest.dto

import dailyquest.preferencequest.entity.PreferenceDetailQuest
import dailyquest.quest.entity.DetailQuestType
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.hibernate.validator.constraints.Range

data class WebDetailQuestRequest(
    @field:NotBlank(message = "{NotBlank.details.title}")
    @field:Size(max = 50, message = "{Size.details.title}")
    override val title: String,
    override val type: DetailQuestType,
    @field:Range(min = 1, max = 255, message = "{Range.details.targetCount}")
    override val targetCount: Int,
    override val count: Int = 0,
): DetailQuestRequest {

    constructor(preferenceDetailQuest: PreferenceDetailQuest) : this(
        title = preferenceDetailQuest.title,
        type = preferenceDetailQuest.type,
        targetCount = preferenceDetailQuest.targetCount
    )
}