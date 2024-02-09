package dailyquest.quest.dto

import dailyquest.preferencequest.entity.PreferenceDetailQuest
import dailyquest.quest.entity.DetailQuest
import dailyquest.quest.entity.DetailQuestState
import dailyquest.quest.entity.DetailQuestType
import dailyquest.quest.entity.Quest
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.hibernate.validator.constraints.Range

data class DetailRequest(
    @field:NotBlank(message = "{NotBlank.details.title}")
    @field:Size(max = 50, message = "{Size.details.title}")
    val title: String,
    val type: DetailQuestType,
    @field:Range(min = 1, max = 255, message = "{Range.details.targetCount}")
    val targetCount: Int,
) {

    constructor(preferenceDetailQuest: PreferenceDetailQuest) : this(
        title = preferenceDetailQuest.title,
        type = preferenceDetailQuest.type,
        targetCount = preferenceDetailQuest.targetCount
    )

    fun mapToEntity(quest: Quest): DetailQuest {
        return DetailQuest(
            title = title,
            type = type,
            state = DetailQuestState.PROCEED,
            targetCount = if (type == DetailQuestType.CHECK) 1 else targetCount,
            quest = quest
        )
    }
}