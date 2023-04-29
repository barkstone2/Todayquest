package todayquest.quest.dto

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import todayquest.common.MessageUtil
import todayquest.quest.entity.Quest
import todayquest.quest.entity.QuestState
import todayquest.quest.entity.QuestType
import todayquest.user.entity.UserInfo
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.ChronoUnit

class QuestRequest(
    title: String,
    description: String,
    details: MutableList<DetailRequest>? = null,
    deadLine: LocalDateTime? = null,
) {

    @NotBlank(message = "{NotBlank.quest.title}")
    @Size(max = 50, message = "{size.quest.title}")
    val title = title

    @Size(max = 300, message = "{Size.quest.description}")
    val description = description

    val deadLine: LocalDateTime? = deadLine

    @Valid
    @Size(max = 5, message = "{Size.quest.details}")
    val details = details ?: mutableListOf()

    private var type: QuestType = QuestType.SUB

    fun toMainQuest() {
        this.type = QuestType.MAIN
    }

    fun mapToEntity(nextSeq: Long, userInfo: UserInfo): Quest {
        return Quest(
            title = title,
            description = description,
            user = userInfo,
            seq = nextSeq,
            state = QuestState.PROCEED,
            type = type,
            deadline = deadLine
        )
    }

    override fun toString(): String {
        return "QuestRequest(title='$title', description='$description', details=$details, type=$type)"
    }

    fun checkRangeOfDeadLine(resetTime: LocalTime) {
        if (deadLine != null) {
            val now = LocalDateTime.now()
            val nextReset = LocalDateTime.of(LocalDate.now().plus(1, ChronoUnit.DAYS), resetTime)
            require(deadLine.isAfter(now) && deadLine.isBefore(nextReset)) { MessageUtil.getMessage("Range.quest.deadLine") }
        }
    }

}
