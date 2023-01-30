package todayquest.quest.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import todayquest.quest.entity.Quest
import todayquest.quest.entity.QuestState
import todayquest.quest.entity.QuestType
import todayquest.user.entity.UserInfo
import java.time.LocalDateTime

data class QuestRequestDto(
    var questId: Long? = null,
    @field:NotBlank @field:Size(max = 50)
    var title: String? = null,

    @field:NotBlank @field:Size(max = 300)
    var description: String? = null,

    var state: QuestState? = null,

    @field:Size(max = 3)
    var rewards: MutableList<Long> = mutableListOf(),

    @field:Size(max = 5)
    var detailQuests: MutableList<DetailQuestRequestDto> = mutableListOf(),
    var lastModifiedDate: LocalDateTime? = null,
    var canComplete : Boolean? = null,
    var type: QuestType = QuestType.SUB
) {

    fun mapToEntity(nextSeq: Long, userInfo: UserInfo): Quest {
        return Quest(
            title = title ?: throw IllegalArgumentException("퀘스트 이름은 비어있을 수 없습니다."),
            description = description,
            user = userInfo,
            seq = nextSeq,
            state = QuestState.PROCEED,
            type = type
            )
    }

}