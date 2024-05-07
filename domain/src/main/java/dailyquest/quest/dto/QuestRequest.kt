package dailyquest.quest.dto

import dailyquest.preferencequest.entity.PreferenceQuest
import dailyquest.quest.entity.Quest
import dailyquest.quest.entity.QuestState
import dailyquest.quest.entity.QuestType
import java.time.LocalDateTime

interface QuestRequest {
    val title: String
    val description: String
    val type: QuestType
    val details: List<DetailQuestRequest>
    val deadLine: LocalDateTime?
    val preferenceQuest: PreferenceQuest?

    fun mapToEntity(nextSeq: Long, userId: Long): Quest {
        val quest = Quest(
            title = title,
            description = description,
            userId = userId,
            seq = nextSeq,
            state = QuestState.PROCEED,
            type = type,
            deadline = deadLine,
            preferenceQuest = preferenceQuest
        )
        quest.replaceDetailQuests(this.details.map { it.mapToEntity(quest) })
        return quest
    }
}