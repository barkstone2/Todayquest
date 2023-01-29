package todayquest.quest.dto

import org.springframework.format.annotation.DateTimeFormat
import todayquest.quest.entity.Quest
import todayquest.quest.entity.QuestDifficulty
import todayquest.quest.entity.QuestState
import todayquest.user.entity.UserInfo
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

data class QuestRequestDto(
    var questId: Long? = null,
    @field:NotBlank @field:Size(max = 50)
    var title: String? = null,

    @field:NotBlank @field:Size(max = 300)
    var description: String? = null,

    @field:NotNull
    var difficulty: QuestDifficulty = QuestDifficulty.EASY,
    var state: QuestState? = null,

    @field:Size(max = 3)
    var rewards: MutableList<Long> = mutableListOf(),

    @field:Size(max = 5)
    var detailQuests: MutableList<DetailQuestRequestDto> = mutableListOf(),
    var lastModifiedDate: LocalDateTime? = null,
    var canComplete : Boolean? = null,
) {

    fun mapToEntity(nextSeq: Long, userInfo: UserInfo): Quest {
        return Quest(
            title = title ?: throw IllegalArgumentException("퀘스트 이름은 비어있을 수 없습니다."),
            description = description,
            user = userInfo,
            seq = nextSeq,
            state = QuestState.PROCEED,
            difficulty = difficulty
            )
    }

}