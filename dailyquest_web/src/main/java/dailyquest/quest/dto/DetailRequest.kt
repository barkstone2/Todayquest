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
    val count: Int = 0,
) {

    constructor(preferenceDetailQuest: PreferenceDetailQuest) : this(
        title = preferenceDetailQuest.title,
        type = preferenceDetailQuest.type,
        targetCount = preferenceDetailQuest.targetCount
    )

    fun mapToEntity(quest: Quest): DetailQuest {
        return DetailQuest.of(
            title = title,
            type = type,
            count = count,
            state = DetailQuestState.PROCEED,
            targetCount = targetCount,
            quest = quest
        )
    }
}