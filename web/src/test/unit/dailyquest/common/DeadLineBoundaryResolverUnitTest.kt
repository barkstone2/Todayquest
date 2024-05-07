package dailyquest.common

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.LocalDateTime
import java.time.ZoneOffset

@DisplayName("데드라인 바운더리 리졸버 단위 테스트")
class DeadLineBoundaryResolverUnitTest {

    private val timeZone = ZoneOffset.ofHours(9)
    private val baseTime = LocalDateTime.of(2022, 12, 12, 12, 0, 0).atZone(timeZone)
    private val clock = Clock.fixed(baseTime.toInstant(), timeZone)

    @DisplayName("minBoundary 요청 시")
    @Nested
    inner class TestMinBoundary {
        @DisplayName("현재 시간 + gapMinutes 시간이 반환된다")
        @Test
        fun `현재 시간 + gapMinutes 시간이 반환된다`() {
            //given
            val now = LocalDateTime.now(clock)
            val gapMinutes = 5L
            val boundaryResolver = DeadLineBoundaryResolver(gapMinutes, clock)

            //when
            val minBoundary = boundaryResolver.resolveMinBoundary()

            //then
            assertThat(minBoundary).isEqualTo(now.plusMinutes(gapMinutes))
        }

        @DisplayName("반환된 시간의 초는 0으로 설정된다")
        @Test
        fun `반환된 시간의 초는 0으로 설정된다`() {
            //given
            val boundaryResolver = DeadLineBoundaryResolver()

            //when
            val minBoundary = boundaryResolver.resolveMinBoundary()

            //then
            assertThat(minBoundary.second).isEqualTo(0)
        }

        @DisplayName("반환된 시간의 나노초는 0으로 설정된다")
        @Test
        fun `반환된 시간의 나노초는 0으로 설정된다`() {
            //given
            val boundaryResolver = DeadLineBoundaryResolver()

            //when
            val minBoundary = boundaryResolver.resolveMinBoundary()

            //then
            assertThat(minBoundary.nano).isEqualTo(0)
        }
    }
    
    @DisplayName("maxBoundary 요청 시")
    @Nested
    inner class TestMaxBoundary {
        private val todaySixAm = LocalDateTime.of(2012, 12, 12, 6, 0, 0).atZone(timeZone).toLocalDateTime()

        @DisplayName("현재 시간이 오전 6시 1분 전이라면 오늘 오전 6시 - gapMinutes가 반환된다")
        @Test
        fun `현재 시간이 오전 6시 1분 전이라면 오늘 오전 6시 - gapMinutes가 반환된다`() {
            //given
            val beforeSixAm = todaySixAm.minusMinutes(1)
            val clock = setClockTo(beforeSixAm)

            val gapMinutes = 5L
            val boundaryResolver = DeadLineBoundaryResolver(gapMinutes, clock)

            //when
            val maxBoundary = boundaryResolver.resolveMaxBoundary()

            //then
            assertThat(maxBoundary).isEqualTo(todaySixAm.minusMinutes(gapMinutes))
        }

        @DisplayName("현재 시간이 오전 6시라면 내일 오전 6시 - gapMinutes가 반환된다")
        @Test
        fun `현재 시간이 오전 6시라면 내일 오전 6시 - gapMinutes가 반환된다`() {
            //given
            val clock = setClockTo(todaySixAm)

            val gapMinutes = 5L
            val boundaryResolver = DeadLineBoundaryResolver(gapMinutes, clock)

            //when
            val maxBoundary = boundaryResolver.resolveMaxBoundary()

            //then
            assertThat(maxBoundary).isEqualTo(todaySixAm.plusDays(1).minusMinutes(gapMinutes))
        }

        @DisplayName("현재 시간이 오전 6시 1분이라면 내일 오전 6시 - gapMinutes가 반환된다")
        @Test
        fun `현재 시간이 오전 6시 1분이라면 내일 오전 6시 - gapMinutes가 반환된다`() {
            //given
            val afterSixAm = todaySixAm.plusMinutes(1)
            val clock = setClockTo(afterSixAm)

            val gapMinutes = 5L
            val boundaryResolver = DeadLineBoundaryResolver(gapMinutes, clock)

            //when
            val maxBoundary = boundaryResolver.resolveMaxBoundary()

            //then
            assertThat(maxBoundary).isEqualTo(todaySixAm.plusDays(1).minusMinutes(gapMinutes))
        }

        @DisplayName("반환된 시간의 초는 0으로 설정된다")
        @Test
        fun `반환된 시간의 초는 0으로 설정된다`() {
            //given
            val boundaryResolver = DeadLineBoundaryResolver()

            //when
            val maxBoundary = boundaryResolver.resolveMaxBoundary()

            //then
            assertThat(maxBoundary.second).isEqualTo(0)
        }

        @DisplayName("반환된 시간의 나노초는 0으로 설정된다")
        @Test
        fun `반환된 시간의 나노초는 0으로 설정된다`() {
            //given
            val boundaryResolver = DeadLineBoundaryResolver()

            //when
            val maxBoundary = boundaryResolver.resolveMaxBoundary()

            //then
            assertThat(maxBoundary.nano).isEqualTo(0)
        }
    }

    private fun setClockTo(now: LocalDateTime): Clock {
        return Clock.fixed(now.toInstant(timeZone), timeZone)
    }
}