package todayquest.quest.dto

import todayquest.common.firstDayOfQuarter
import todayquest.common.lastDayOfQuarter
import todayquest.quest.entity.QuestState
import todayquest.quest.entity.QuestType
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Period
import java.time.temporal.TemporalAdjusters
import java.util.*
import java.util.function.Function
import java.util.stream.Collectors


class QuestLogSearchCondition(
    var searchType: QuestLogSearchType = QuestLogSearchType.DAILY,
    private var startDate: LocalDate = LocalDate.now()
) {

    fun getSelectedDate() : LocalDate {
        return startDate
    }

    fun getStartDate() : LocalDateTime {

        return when (searchType) {
            QuestLogSearchType.WEEKLY -> startDate.firstDayOfQuarter()
            QuestLogSearchType.MONTHLY -> startDate.with(TemporalAdjusters.firstDayOfYear()).atStartOfDay()
            else -> startDate.with(TemporalAdjusters.firstDayOfMonth()).atStartOfDay()
        }
    }

    fun getEndDate() : LocalDateTime {
        return when (searchType) {
            QuestLogSearchType.WEEKLY -> startDate.lastDayOfQuarter()
            QuestLogSearchType.MONTHLY -> startDate.with(TemporalAdjusters.lastDayOfYear()).atTime(23, 59, 59)
            else -> startDate.with(TemporalAdjusters.lastDayOfMonth()).atTime(23, 59, 59)
        }
    }

    private fun getPeriodDependingOnType() : Period{
        return when(searchType) {
            QuestLogSearchType.WEEKLY -> Period.ofWeeks(1)
            QuestLogSearchType.MONTHLY -> Period.ofMonths(1)
            else -> Period.ofDays(1)
        }
    }

    fun createResponseCollectionByType() : MutableMap<LocalDate, Map<String, Long>> {

        val countMap = Arrays
            .stream(QuestType.values())
            .collect(
                Collectors.toMap(QuestType::name) { _ -> 0L }
            )

        val startDayFromCondition = LocalDate.from(this.getStartDate())
        val endDayFromCondition = LocalDate.from(this.getEndDate())

        return startDayFromCondition
            .datesUntil(endDayFromCondition.plusDays(1), getPeriodDependingOnType())
            .collect(
                Collectors.toMap(
                    Function.identity(),
                    Function { countMap.toMutableMap() },
                )
            )
    }

    fun createResponseCollectionByState() : MutableMap<LocalDate, Map<String, Long>> {

        val countMap = Arrays
            .stream(QuestState.values())
            .collect(
                Collectors.toMap(QuestState::name) { _ -> 0L }
            )

        val startDayFromCondition = LocalDate.from(this.getStartDate())
        val endDayFromCondition = LocalDate.from(this.getEndDate())

        return startDayFromCondition
            .datesUntil(endDayFromCondition.plusDays(1), getPeriodDependingOnType())
            .collect(
                Collectors.toMap(
                    Function.identity(),
                    Function { countMap.toMutableMap() },
                )
            )
    }

}
