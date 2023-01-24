package todayquest.quest.dto

import todayquest.quest.entity.DetailQuest
import todayquest.quest.entity.DetailQuestState
import todayquest.quest.entity.DetailQuestType
import todayquest.quest.entity.Quest

data class DetailQuestRequestDto(
    var id: Long? = null,
    var title: String? = null,
    var targetCount: Short? = null,
    var count: Short? = null,
    var type: DetailQuestType? = null,
    var state: DetailQuestState? = DetailQuestState.PROCEED,
) {

    fun mapToEntity(quest: Quest): DetailQuest {
        return DetailQuest(
            title = title!!,
            type = type!!,
            state = DetailQuestState.PROCEED,
            targetCount = if (type == DetailQuestType.CHECK) 1 else targetCount!!,
            quest = quest
        )
    }
}