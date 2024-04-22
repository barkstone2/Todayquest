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