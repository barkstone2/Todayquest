package todayquest.quest.dto

import org.springframework.format.annotation.DateTimeFormat
import todayquest.quest.entity.*
import todayquest.reward.dto.RewardResponseDto
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

data class QuestResponseDto(
    var questId: Long? = null,
    var title: String? = null,
    var description: String? = null,
    var seq: Long? = null,
    var isRepeat: Boolean = false,
    @field:DateTimeFormat(pattern = "yyyy-MM-dd")
    var deadLineDate: LocalDate? = null,
    @field:DateTimeFormat(pattern = "HH:mm")
    var deadLineTime: LocalTime? = null,
    var state: QuestState? = null,
    var type: QuestType? = null,
    var difficulty: QuestDifficulty? = null,
    var rewards: List<RewardResponseDto>? = null,
    var lastModifiedDate: LocalDateTime? = null,
    var detailQuests: List<DetailQuestResponseDto>? = null,
    var canComplete : Boolean? = null,
) {

    companion object {
        @JvmStatic
        fun createDto(quest: Quest): QuestResponseDto {
            return QuestResponseDto(
                questId = quest.id,
                title = quest.title,
                description = quest.description,
                seq = quest.seq,
                isRepeat = quest.isRepeat,
                deadLineDate = quest.deadLineDate,
                deadLineTime = quest.deadLineTime,
                state = quest.state,
                type = quest.type,
                difficulty = quest.difficulty,
                lastModifiedDate = quest.lastModifiedDate,
                rewards = quest.rewards.map {
                    RewardResponseDto.createDto(
                        it.reward
                    )
                }.toCollection(mutableListOf()),
                detailQuests = quest.detailQuests.map {
                    DetailQuestResponseDto.createDto(
                        it
                    )
                }.toCollection(mutableListOf()),
                canComplete = quest.detailQuests.all { dq -> dq.state == DetailQuestState.COMPLETE }
            )
        }
    }

}