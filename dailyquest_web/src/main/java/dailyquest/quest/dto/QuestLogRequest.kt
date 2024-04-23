package dailyquest.quest.dto

import dailyquest.quest.entity.Quest
import dailyquest.quest.entity.QuestLog
import dailyquest.quest.entity.QuestState
import dailyquest.quest.entity.QuestType
import java.time.LocalDate
import java.time.LocalTime

class QuestLogRequest private constructor(
    val userId: Long,
    val questId: Long,
    val state: QuestState,
    val type: QuestType,
    val loggedDate: LocalDate,
) {
    fun mapToEntity(): QuestLog {
        return QuestLog(userId, questId, state, type, loggedDate)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as QuestLogRequest

        if (userId != other.userId) return false
        if (questId != other.questId) return false
        if (state != other.state) return false
        if (type != other.type) return false
        if (loggedDate != other.loggedDate) return false

        return true
    }

    override fun hashCode(): Int {
        var result = userId.hashCode()
        result = 31 * result + questId.hashCode()
        result = 31 * result + state.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + loggedDate.hashCode()
        return result
    }

    companion object {
        @JvmStatic
        fun from(quest: Quest): QuestLogRequest {
            val createdDate = quest.createdDate.toLocalDate()
            val nowTime = quest.createdDate.toLocalTime()
            val resetTime = LocalTime.of(6, 0)
            val loggedDate = if (nowTime.isBefore(resetTime)) createdDate.minusDays(1L) else createdDate
            return QuestLogRequest(quest.userId, quest.id, quest.state, quest.type, loggedDate)
        }
    }
}