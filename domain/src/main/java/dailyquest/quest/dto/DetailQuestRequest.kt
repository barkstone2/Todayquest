package dailyquest.quest.dto

import dailyquest.quest.entity.DetailQuest
import dailyquest.quest.entity.DetailQuestState
import dailyquest.quest.entity.DetailQuestType
import dailyquest.quest.entity.Quest

interface DetailQuestRequest {
    val title: String
    val type: DetailQuestType
    val targetCount: Int
    val count: Int

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