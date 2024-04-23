package dailyquest.validation.validator

import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@DisplayName("데드라인 범위 검증기 유닛 테스트")
class DeadLineRangeValidatorUnitTest {

    private val validator = DeadLineRangeValidator()

    @DisplayName("검증기 동작 시")
    @Nested
    inner class TestIsValid {
        private val today = LocalDate.of(2020, 12, 12)
        private val now = LocalDateTime.of(today, LocalTime.of(15, 0))
        private val nextReset = LocalDateTime.of(today.plusDays(1), LocalTime.of(6, 0))

        @BeforeEach
        fun init() {
            mockkStatic(LocalDateTime::class)
            every { LocalDateTime.now() } returns now
        }

        @AfterEach
        fun destroy() {
            unmockkStatic(LocalDateTime::class)
        }

        @DisplayName("검증값이 null이면 true를 반환한다")
        @Test
        fun `검증값이 null이면 true를 반환한다`() {
            //when
            val result = validator.isValid(null, null)

            //then
            assertThat(result).isTrue()
        }
        
        @DisplayName("검증값이 현재 시간 +5분 이전이면 false를 반환한다")
        @Test
        fun `검증값이 현재 시간 +5분 이전이면 false를 반환한다`() {
            //given
            val targetValue = now.plusMinutes(5).minusSeconds(1)

            //when
            val result = validator.isValid(targetValue, null)

            //then
            assertThat(result).isFalse()
        }
        
        @DisplayName("검증값이 현재 시간 +5분과 동일하면 false를 반환한다")
        @Test
        fun `검증값이 현재 시간 +5분과 동일하면 false를 반환한다`() {
            //given
            val targetValue = now.plusMinutes(5)

            //when
            val result = validator.isValid(targetValue, null)

            //then
            assertThat(result).isFalse()
        }

        @DisplayName("검증값이 현재 시간 +5분 이후면 true를 반환한다")
        @Test
        fun `검증값이 현재 시간 +5분 이후면 true를 반환한다`() {
            //given
            val targetValue = now.plusMinutes(5).plusSeconds(1)

            //when
            val result = validator.isValid(targetValue, null)

            //then
            assertThat(result).isTrue()
        }

        @DisplayName("검증값이 다음 오전 6시 -5분 이후면 false를 반환한다")
        @Test
        fun `검증값이 다음 오전 6시 -5분 이후면 false를 반환한다`() {
            //given
            val targetValue = nextReset.minusMinutes(5).plusSeconds(1)

            //when
            val result = validator.isValid(targetValue, null)

            //then
            assertThat(result).isFalse()
        }
        
        @DisplayName("검증값이 다음 오전 6시 -5분과 동일하면 false를 반환한다")
        @Test
        fun `검증값이 다음 오전 6시 -5분과 동일하면 false를 반환한다`() {
            //given
            val targetValue = nextReset.minusMinutes(5)

            //when
            val result = validator.isValid(targetValue, null)

            //then
            assertThat(result).isFalse()
        }

        @DisplayName("검증값이 다음 오전 6시 -5분 이전이면 true를 반환한다")
        @Test
        fun `검증값이 다음 오전 6시 -5분 이전이면 true를 반환한다`() {
            //given
            val targetValue = nextReset.minusMinutes(5).minusSeconds(1)

            //when
            val result = validator.isValid(targetValue, null)

            //then
            assertThat(result).isTrue()
        }
    }
}