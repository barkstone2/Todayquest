package dailyquest.quest.dto

import dailyquest.common.firstDayOfQuarter
import dailyquest.common.lastDayOfQuarter
import java.time.LocalDate
import java.time.Period
import java.time.temporal.TemporalAdjusters


class QuestLogSearchCondition(
    val searchType: QuestLogSearchType = QuestLogSearchType.DAILY,
    val selectedDate: LocalDate = LocalDate.now()
) {

    fun getStartDateOfSearchRange() : LocalDate {

        return when (searchType) {
            QuestLogSearchType.WEEKLY -> selectedDate.firstDayOfQuarter()
            QuestLogSearchType.MONTHLY -> selectedDate.with(TemporalAdjusters.firstDayOfYear())
            else -> selectedDate.with(TemporalAdjusters.firstDayOfMonth())
        }
    }

    fun getEndDateOfSearchRange() : LocalDate {
        return when (searchType) {
            QuestLogSearchType.WEEKLY -> selectedDate.lastDayOfQuarter()
            QuestLogSearchType.MONTHLY -> selectedDate.with(TemporalAdjusters.lastDayOfYear())
            else -> selectedDate.with(TemporalAdjusters.lastDayOfMonth())
        }
    }

    private fun getPeriodUnitOfSearchType() : Period{
        return when(searchType) {
            QuestLogSearchType.WEEKLY -> Period.ofWeeks(1)
            QuestLogSearchType.MONTHLY -> Period.ofMonths(1)
            else -> Period.ofDays(1)
        }
    }

    fun createResponseMapOfPeriodUnit() : Map<LocalDate, QuestStatisticsResponse> {
        val startDateOfSearchRange = getStartDateOfSearchRange()
        val endDateOfSearchRange = getEndDateOfSearchRange()

        val responseMap: MutableMap<LocalDate, QuestStatisticsResponse> = mutableMapOf()

        startDateOfSearchRange
            .datesUntil(endDateOfSearchRange.plusDays(1), getPeriodUnitOfSearchType())
            .forEach { responseMap[it] = QuestStatisticsResponse(it) }

        return responseMap
    }

}
