package todayquest.quest.dto

import todayquest.quest.entity.DetailQuest
import todayquest.quest.entity.DetailQuestState
import todayquest.quest.entity.DetailQuestType

data class DetailResponse(
    val id: Long? = null,
    val title: String? = null,
    val targetCount: Int? = null,
    val count: Int? = null,
    val type: DetailQuestType? = null,
    val state: DetailQuestState? = null,
    var canCompleteParent: Boolean? = false,
) {

    companion object {
        fun createDto(dq: DetailQuest, canCompleteParent: Boolean? = false): DetailResponse {
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