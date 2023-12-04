package dailyquest.batch

import dailyquest.batch.job.DeadLineStepListener
import dailyquest.quest.entity.Quest
import dailyquest.quest.entity.QuestLog
import dailyquest.quest.repository.QuestLogRepository
import dailyquest.search.repository.QuestIndexRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Answers
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.springframework.batch.item.Chunk
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@DisplayName("데드라인 스텝 리스너 단위 테스트")
class DeadLineStepListenerUnitTest {

    private val questLogRepository: QuestLogRepository = mock<QuestLogRepository>()
    private val questIndexRepository: QuestIndexRepository  = mock<QuestIndexRepository>()
    private lateinit var deadLineStepListener: DeadLineStepListener

    @DisplayName("afterWrite 동작 시")
    @Nested
    inner class TestAfterWrite {
        @DisplayName("targetDate의 시간이 오전 6시 이전이라면, 어제 날짜가 loggedDate로 사용된다")
        @Test
        fun `targetDate의 시간이 오전 6시 이전이라면, 어제 날짜가 loggedDate로 사용된다`() {
            //given
            val targetDateStr = "2022-12-01 05:59:00"
            val loggedDate = LocalDate.parse(targetDateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                .minusDays(1)

            deadLineStepListener = DeadLineStepListener(questLogRepository, questIndexRepository, targetDateStr)
            val questLogListArgumentCaptor = argumentCaptor<MutableList<QuestLog>>()

            val mockQuest = mock(Quest::class.java, Answers.RETURNS_DEEP_STUBS)

            //when
            deadLineStepListener.afterWrite(Chunk<Quest>(mockQuest))

            //then
            verify(questLogRepository).saveAll(questLogListArgumentCaptor.capture())
            val questLogList = questLogListArgumentCaptor.firstValue
            assertThat(questLogList).allMatch { it.loggedDate == loggedDate }
        }

        @DisplayName("targetDate의 시간이 오전 6시와 같다면, 현재 날짜가 loggedDate로 사용된다")
        @Test
        fun `targetDate의 시간이 오전 6시와 같다면, 현재 날짜가 loggedDate로 사용된다`() {
            //given
            val targetDateStr = "2022-12-01 06:00:00"
            val loggedDate = LocalDate.parse(targetDateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

            deadLineStepListener = DeadLineStepListener(questLogRepository, questIndexRepository, targetDateStr)
            val questLogListArgumentCaptor = argumentCaptor<MutableList<QuestLog>>()

            val mockQuest = mock(Quest::class.java, Answers.RETURNS_DEEP_STUBS)

            //when
            deadLineStepListener.afterWrite(Chunk<Quest>(mockQuest))

            //then
            verify(questLogRepository).saveAll(questLogListArgumentCaptor.capture())
            val questLogList = questLogListArgumentCaptor.firstValue
            assertThat(questLogList).allMatch { it.loggedDate == loggedDate }
        }

        @DisplayName("targetDate의 시간이 오전 6시 이후라면, 현재 날짜가 loggedDate로 사용된다")
        @Test
        fun `targetDate의 시간이 오전 6시 이후라면, 현재 날짜가 loggedDate로 사용된다`() {
            //given
            val targetDateStr = "2022-12-01 06:01:00"
            val loggedDate = LocalDate.parse(targetDateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

            deadLineStepListener = DeadLineStepListener(questLogRepository, questIndexRepository, targetDateStr)
            val questLogListArgumentCaptor = argumentCaptor<MutableList<QuestLog>>()

            val mockQuest = mock(Quest::class.java, Answers.RETURNS_DEEP_STUBS)

            //when
            deadLineStepListener.afterWrite(Chunk<Quest>(mockQuest))

            //then
            verify(questLogRepository).saveAll(questLogListArgumentCaptor.capture())
            val questLogList = questLogListArgumentCaptor.firstValue
            assertThat(questLogList).allMatch { it.loggedDate == loggedDate }
        }
    }
}