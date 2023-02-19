package todayquest.quest.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.hibernate.validator.constraints.Range
import todayquest.quest.entity.DetailQuest
import todayquest.quest.entity.DetailQuestState
import todayquest.quest.entity.DetailQuestType
import todayquest.quest.entity.Quest

class DetailRequest(
    title: String,
    type: DetailQuestType,
    targetCount: Int,
    id: Long? = null,
) {

    var id = id

    @NotBlank(message = "{NotBlank.details.title}")
    @Size(max = 50, message = "{Size.details.title}")
    var title = title

    var type = type

    @Range(min = 1, max = 255, message = "{Range.details.targetCount}")
    var targetCount = targetCount

    fun mapToEntity(quest: Quest): DetailQuest {

        return DetailQuest(
            title = title,
            type = type,
            state = DetailQuestState.PROCEED,
            targetCount = if (type == DetailQuestType.CHECK) 1 else targetCount,
            quest = quest
        )
    }

    override fun toString(): String {
        return "DetailRequest(id=$id, title='$title', type=$type, targetCount=$targetCount)"
    }

}