package dailyquest.quest.dto

import dailyquest.preferencequest.entity.PreferenceQuest
import dailyquest.quest.entity.QuestType
import java.time.LocalDateTime

data class SimpleQuestRequest(
    override val title: String,
    override val description: String = "",
    override val type: QuestType = QuestType.SUB,
    override val details: List<DetailQuestRequest> = emptyList(),
    override val deadLine: LocalDateTime? = null,
    override val preferenceQuest: PreferenceQuest? = null,
) : QuestRequest {

    companion object {
        @JvmStatic
        fun from(preferenceQuest: PreferenceQuest): QuestRequest {
            return SimpleQuestRequest(
                title = preferenceQuest.title,
                description = preferenceQuest.description,
                details = preferenceQuest.preferenceDetailQuests.map { SimpleDetailQuestRequest.from(it) },
                preferenceQuest = preferenceQuest
            )
        }
    }
}