package dailyquest.common

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters


fun LocalDate.firstDayOfQuarter(): LocalDate {
    val week = (this.dayOfYear-1) / 7
    val quarter = week / 13
    val weeks = quarter * 13L
    val startWeek = this.withMonth(1).withDayOfMonth(1).plusWeeks(weeks)

    return startWeek.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
}

fun LocalDate.lastDayOfQuarter(): LocalDate {
    val week = (this.dayOfYear-1) / 7
    val quarter = week / 13
    val weeks = (quarter + 1) * 13L
    val endWeek = this.withMonth(1).withDayOfMonth(1).plusWeeks(weeks -1)

    return endWeek.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
}

fun LocalDate.firstDayOfWeek() : LocalDate {
    return LocalDate.from(this.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)))
}

fun LocalDate.firstDayOfMonth() : LocalDate {
    return LocalDate.from(this.with(TemporalAdjusters.firstDayOfMonth()))
}