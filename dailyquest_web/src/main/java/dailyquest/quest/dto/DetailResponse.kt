package dailyquest.quest.dto

import dailyquest.quest.entity.DetailQuest
import dailyquest.quest.entity.DetailQuestState
import dailyquest.quest.entity.DetailQuestType

data class DetailResponse(
    val id: Long = 0,
    val title: String = "",
    val targetCount: Int = 1,
    val count: Int = 0,
    val type: DetailQuestType = DetailQuestType.CHECK,
    val state: DetailQuestState = DetailQuestState.PROCEED,
    val canCompleteParent: Boolean = false,
) {

    companion object {
        fun createDto(dq: DetailQuest, canCompleteParent: Boolean = false): DetailResponse {
            return DetailResponse(
                id = dq.id,
                title = dq.title,
                targetCount = dq.targetCount,
                count = dq.count,
                type = dq.type,
                state = dq.state,
                canCompleteParent = canCompleteParent
            )
        }
    }
}