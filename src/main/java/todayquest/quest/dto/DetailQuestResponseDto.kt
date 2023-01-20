package todayquest.quest.dto

import todayquest.quest.entity.DetailQuest
import todayquest.quest.entity.DetailQuestState
import todayquest.quest.entity.DetailQuestType

data class DetailQuestResponseDto(
    val title: String? = null,
    var targetCount: Short? = null,
    var count: Short? = null,
    var type: DetailQuestType? = null,
    var state: DetailQuestState? = null,
) {

    companion object {
        fun createDto(dq: DetailQuest): DetailQuestResponseDto {
            return DetailQuestResponseDto(
                title = dq.title,
                targetCount = dq.targetCount,
                count = dq.count,
                type = dq.type,
                state = dq.state
            )
        }
    }
}