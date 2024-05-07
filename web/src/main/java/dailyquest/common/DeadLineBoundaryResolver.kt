
package dailyquest.common

import java.time.Clock
import java.time.LocalDateTime

class DeadLineBoundaryResolver(
    private val gap: Long = 5L,
    private val clock: Clock = Clock.systemDefaultZone(),
) {

    fun resolveMinBoundary(): LocalDateTime {
        return this.nowWithoutSecondAndNano().plusMinutes(gap)
    }

    private fun nowWithoutSecondAndNano(): LocalDateTime {
        return LocalDateTime.now(clock).withSecond(0).withNano(0)
    }

    fun resolveMaxBoundary(): LocalDateTime {
        return nextSixAm().minusMinutes(gap)
    }

    private fun nextSixAm(): LocalDateTime {
        val now = nowWithoutSecondAndNano()
        val sixAm = now.withHour(6).withMinute(0)
        return if (now.isBefore(sixAm)) sixAm else sixAm.plusDays(1)
    }
}