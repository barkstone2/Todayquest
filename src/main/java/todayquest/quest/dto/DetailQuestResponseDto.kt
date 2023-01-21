package todayquest.quest.dto

import todayquest.quest.entity.DetailQuest
import todayquest.quest.entity.DetailQuestState
import todayquest.quest.entity.DetailQuestType

data class DetailQuestResponseDto(
    val id: Long? = null,
    val title: String? = null,
    val targetCount: Short? = null,
    val count: Short? = null,
    val type: DetailQuestType? = null,
    val state: DetailQuestState? = null,
) {

    companion object {
        fun createDto(dq: DetailQuest): DetailQuestResponseDto {
            return DetailQuestResponseDto(
                id = dq.id,
                title = dq.title,
                targetCount = dq.targetCount,
                count = dq.count,
                type = dq.type,
                state = dq.state
            )
        }
    }
}