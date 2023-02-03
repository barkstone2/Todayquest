package todayquest.common

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.TemporalAdjusters


fun LocalDate.firstDayOfQuarter() : LocalDateTime {
    val quarter = (this.monthValue - 1) / 3 + 1
    val startMonth = (quarter - 1) * 3 + 1
    val startOfQuarter = this.withMonth(startMonth).withDayOfMonth(1)

    return startOfQuarter.atTime(0, 0, 0)
}

fun LocalDate.lastDayOfQuarter() : LocalDateTime {
    val quarter = (this.monthValue - 1) / 3 + 1
    val endMonth = quarter * 3
    val endOfQuarter = this.withMonth(endMonth).withDayOfMonth(1).plusMonths(1).minusDays(1)

    return endOfQuarter.atTime(23, 59, 59)
}

fun LocalDateTime.firstDayOfWeek() : LocalDate {
    return LocalDate.from(this.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)))
}