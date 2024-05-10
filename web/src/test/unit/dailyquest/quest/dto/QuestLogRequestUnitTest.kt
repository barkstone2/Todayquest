package dailyquest.quest.dto

import dailyquest.quest.entity.Quest
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@ExtendWith(MockKExtension::class)
@DisplayName("퀘스트 로그 요청 DTO 유닛 테스트")
class QuestLogRequestUnitTest {

    @DisplayName("팩토리 메서드 호출 시")
    @Nested
    inner class TestFactoryMethod {
        @RelaxedMockK
        private lateinit var quest: Quest
        private val lastModifiedDate = LocalDate.of(2020, 12, 12)

        @DisplayName("최종 수정일 시간이 오전 6시 1분이라면, loggedDate가 최종 수정일과 동일하다")
        @Test
        fun `최종 수정일 시간이 오전 6시 1분이라면, loggedDate가 최종 수정일과 동일하다`() {
            //given
            val lastModifiedDateTime = LocalDateTime.of(lastModifiedDate, LocalTime.of(6, 1))
            every { quest.lastModifiedDate } returns lastModifiedDateTime

            //when
            val questLogRequest = QuestLogRequest.from(quest)

            //then
            assertThat(questLogRequest.loggedDate).isEqualTo(lastModifiedDate)
        }

        @DisplayName("최종 수정일 시간이 오전 6시라면, loggedDate가 최종 수정일과 동일하다")
        @Test
        fun `최종 수정일 시간이 오전 6시라면, loggedDate가 최종 수정일과 동일하다`() {
            //given
            val lastModifiedDateTime = LocalDateTime.of(lastModifiedDate, LocalTime.of(6, 0))
            every { quest.lastModifiedDate } returns lastModifiedDateTime

            //when
            val questLogRequest = QuestLogRequest.from(quest)

            //then
            assertThat(questLogRequest.loggedDate).isEqualTo(lastModifiedDate)
        }

        @DisplayName("최종 수정일 시간이 오전 5시 59분이라면, loggedDate가 최종 수정일 - 1일이다")
        @Test
        fun `최종 수정일 시간이 오전 5시 59분이라면, loggedDate가 최종 수정일 - 1일이다`() {
            //given
            val lastModifiedDateTime = LocalDateTime.of(lastModifiedDate, LocalTime.of(5, 59))
            every { quest.lastModifiedDate } returns lastModifiedDateTime

            //when
            val questLogRequest = QuestLogRequest.from(quest)

            //then
            assertThat(questLogRequest.loggedDate).isEqualTo(lastModifiedDate.minusDays(1))
        }

        @DisplayName("최종 수정일 시간이 오후 11시 59분이라면, loggedDate가 최종 수정일과 동일하다")
        @Test
        fun `최종 수정일 시간이 오후 11시 59분이라면, loggedDate가 최종 수정일과 동일하다`() {
            //given
            val lastModifiedDateTime = LocalDateTime.of(lastModifiedDate, LocalTime.of(23, 59))
            every { quest.lastModifiedDate } returns lastModifiedDateTime

            //when
            val questLogRequest = QuestLogRequest.from(quest)

            //then
            assertThat(questLogRequest.loggedDate).isEqualTo(lastModifiedDate)
        }

        @DisplayName("최종 수정일 시간이 오전 0시라면, loggedDate가 최종 수정일 -1일이다")
        @Test
        fun `최종 수정일 시간이 오전 0시라면, loggedDate가 최종 수정일 -1일이다`() {
            //given
            val lastModifiedDateTime = LocalDateTime.of(lastModifiedDate, LocalTime.of(0, 0))
            every { quest.lastModifiedDate } returns lastModifiedDateTime

            //when
            val questLogRequest = QuestLogRequest.from(quest)

            //then
            assertThat(questLogRequest.loggedDate).isEqualTo(lastModifiedDate.minusDays(1))
        }

        @DisplayName("최종 수정일 시간이 오전 0시 1분이라면, loggedDate가 최종 수정일 -1일이다")
        @Test
        fun `최종 수정일 시간이 오전 0시 1분이라면, loggedDate가 최종 수정일 -1일이다`() {
            //given
            val lastModifiedDateTime = LocalDateTime.of(lastModifiedDate, LocalTime.of(0, 1))
            every { quest.lastModifiedDate } returns lastModifiedDateTime

            //when
            val questLogRequest = QuestLogRequest.from(quest)

            //then
            assertThat(questLogRequest.loggedDate).isEqualTo(lastModifiedDate.minusDays(1))
        }
    }
}