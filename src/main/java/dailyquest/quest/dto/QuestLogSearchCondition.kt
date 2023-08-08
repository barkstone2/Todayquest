package dailyquest.quest.dto

import dailyquest.common.firstDayOfQuarter
import dailyquest.common.lastDayOfQuarter
import dailyquest.quest.entity.QuestState
import dailyquest.quest.entity.QuestType
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Period
import java.time.temporal.TemporalAdjusters
import java.util.*
import java.util.function.Function
import java.util.stream.Collectors


class QuestLogSearchCondition(
    var searchType: QuestLogSearchType = QuestLogSearchType.DAILY,
    val startDate: LocalDate = LocalDate.now()
) {

    fun getSelectedDate() : LocalDate {
        return startDate
    }

    fun getStartDateOfSearchRange() : LocalDate {

        return when (searchType) {
            QuestLogSearchType.WEEKLY -> startDate.firstDayOfQuarter()
            QuestLogSearchType.MONTHLY -> startDate.with(TemporalAdjusters.firstDayOfYear())
            else -> startDate.with(TemporalAdjusters.firstDayOfMonth())
        }
    }

    fun getEndDateOfSearchRange() : LocalDate {
        return when (searchType) {
            QuestLogSearchType.WEEKLY -> startDate.lastDayOfQuarter()
            QuestLogSearchType.MONTHLY -> startDate.with(TemporalAdjusters.lastDayOfYear())
            else -> startDate.with(TemporalAdjusters.lastDayOfMonth())
        }
    }

    private fun getPeriodUnitOfSearchType() : Period{
        return when(searchType) {
            QuestLogSearchType.WEEKLY -> Period.ofWeeks(1)
            QuestLogSearchType.MONTHLY -> Period.ofMonths(1)
            else -> Period.ofDays(1)
        }
    }

}
