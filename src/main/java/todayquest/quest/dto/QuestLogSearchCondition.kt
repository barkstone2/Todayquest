package todayquest.quest.dto

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.TemporalAdjusters


class QuestLogSearchCondition(
    private var searchType: QuestLogSearchType = QuestLogSearchType.DAILY,
    private var startDate: LocalDate = LocalDate.now()
) {

    fun getStartDate() : LocalDateTime {
        return when (searchType) {
            QuestLogSearchType.WEEKLY -> startDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).atStartOfDay()
            QuestLogSearchType.MONTHLY -> startDate.with(TemporalAdjusters.firstDayOfMonth()).atStartOfDay()
            QuestLogSearchType.YEARLY -> startDate.with(TemporalAdjusters.firstDayOfYear()).atStartOfDay()
            else -> startDate.atStartOfDay()
        }
    }

    fun getEndDate() : LocalDateTime {
        return when (searchType) {
            QuestLogSearchType.WEEKLY -> startDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).plusDays(6).atTime(23, 59, 59)
            QuestLogSearchType.MONTHLY -> startDate.with(TemporalAdjusters.lastDayOfMonth()).atTime(23, 59, 59)
            QuestLogSearchType.YEARLY -> startDate.with(TemporalAdjusters.lastDayOfYear()).atTime(23, 59, 59)
            else -> startDate.atTime(23, 59, 59)
        }
    }

}
