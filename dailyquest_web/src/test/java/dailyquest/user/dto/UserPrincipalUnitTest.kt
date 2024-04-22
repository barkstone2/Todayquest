package dailyquest.user.dto

import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@DisplayName("유저 인증 DTO 유닛 테스트")
class UserPrincipalUnitTest {

    @DisplayName("코어타임 확인 로직 호출시")
    @Nested
    inner class IsNowCoreTimeTest {
        private val today: LocalDate = LocalDate.of(2020, 12, 12)
        private val coreTime = LocalTime.of(8, 0)

        @BeforeEach
        fun init() {
            mockkStatic(LocalDateTime::class)
            mockkStatic(LocalDate::class)
            every { LocalDate.now() } returns today
        }

        @AfterEach
        fun destroy() {
            unmockkStatic(LocalDate::class)
        }

        @DisplayName("현재 시간이 코어타임 시간과 동일한 경우 true를 반환한다")
        @Test
        fun `현재 시간이 코어타임 시간과 동일한 경우 true를 반환한다`() {
            //given
            every { LocalDateTime.now() } returns LocalDateTime.of(today, coreTime)
            val userPrincipal = UserPrincipal(1L, "", coreTimeHour = coreTime.hour)

            //when
            val isNowCoreTime = userPrincipal.isNowCoreTime()

            //then
            assertThat(isNowCoreTime).isTrue()
        }
        
        @DisplayName("현재 시간이 코어타임 +1 시간과 동일한 경우 true를 반환한다")
        @Test
        fun `현재 시간이 코어타임 +1 시간과 동일한 경우 true를 반환한다`() {
            //given
            every { LocalDateTime.now() } returns LocalDateTime.of(today, coreTime.plusHours(1))
            val userPrincipal = UserPrincipal(1L, "", coreTimeHour = coreTime.hour)

            //when
            val isNowCoreTime = userPrincipal.isNowCoreTime()

            //then
            assertThat(isNowCoreTime).isTrue()
        }

        @DisplayName("현재 시간이 코어타임 시간 이전인 경우 false를 반환한다")
        @Test
        fun `현재 시간이 코어타임 시간 이전인 경우 false를 반환한다`() {
            //given
            every { LocalDateTime.now() } returns LocalDateTime.of(today, coreTime.minusSeconds(1))
            val userPrincipal = UserPrincipal(1L, "", coreTimeHour = coreTime.hour)

            //when
            val isNowCoreTime = userPrincipal.isNowCoreTime()

            //then
            assertThat(isNowCoreTime).isFalse()
        }

        @DisplayName("현재 시간이 코어타임+1 시간 이후인 경우 false를 반환한다")
        @Test
        fun `현재 시간이 코어타임+1 시간 이후인 경우 false를 반환한다`() {
            //given
            every { LocalDateTime.now() } returns LocalDateTime.of(today, coreTime.plusHours(1).plusSeconds(1))
            val userPrincipal = UserPrincipal(1L, "", coreTimeHour = coreTime.hour)

            //when
            val isNowCoreTime = userPrincipal.isNowCoreTime()

            //then
            assertThat(isNowCoreTime).isFalse()
        }

        @DisplayName("현재 시간이 코어타임과 코어타임+1시간 이내인 경우 true를 반환한다")
        @Test
        fun `현재 시간이 코어타임과 코어타임+1시간 이내인 경우 true를 반환한다`() {
            //given
            every { LocalDateTime.now() } returns LocalDateTime.of(today, coreTime.plusMinutes(30))
            val userPrincipal = UserPrincipal(1L, "", coreTimeHour = coreTime.hour)

            //when
            val isNowCoreTime = userPrincipal.isNowCoreTime()

            //then
            assertThat(isNowCoreTime).isTrue()
        }
    }
}