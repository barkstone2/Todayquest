package dailyquest.common

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjuster
import java.time.temporal.TemporalAdjusters

private val firstMondayOfYearAdjusters: TemporalAdjuster = TemporalAdjusters.dayOfWeekInMonth(1, DayOfWeek.MONDAY)

fun LocalDate.firstDayOfQuarter(): LocalDate {

    val firstMondayOfYear = this.with(firstMondayOfYearAdjusters)

    if(this.isBefore(firstMondayOfYear)) {
        val previousMonday = this.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        return previousMonday.minusWeeks(12)
    }

    val weekDiff = (this.dayOfYear - firstMondayOfYear.dayOfYear) / 7

    val quarter = (weekDiff - 1) / 13
    val weeks = quarter * 13L

    return firstMondayOfYear.plusWeeks(weeks)
}

fun LocalDate.lastDayOfQuarter(): LocalDate {

    val firstMondayOfYear = this.with(firstMondayOfYearAdjusters)
    if(this.isBefore(firstMondayOfYear)) {
        return firstMondayOfYear.minusDays(1)
    }

    val weekDiff = (this.dayOfYear - firstMondayOfYear.dayOfYear) / 7

    val quarter = (weekDiff - 1) / 13
    val weeks = (quarter + 1) * 13L

    return firstMondayOfYear.plusWeeks(weeks).minusDays(1)
}

fun LocalDate.firstDayOfWeek() : LocalDate {
    return LocalDate.from(this.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)))
}

fun LocalDate.firstDayOfMonth() : LocalDate {
    return LocalDate.from(this.with(TemporalAdjusters.firstDayOfMonth()))
}