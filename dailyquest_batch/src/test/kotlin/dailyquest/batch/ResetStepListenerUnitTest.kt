package dailyquest.batch

import dailyquest.batch.job.ResetStepListener
import dailyquest.quest.entity.Quest
import dailyquest.quest.entity.QuestLog
import dailyquest.quest.repository.QuestLogRepository
import dailyquest.search.repository.QuestIndexRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Answers
import org.mockito.Mockito
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.springframework.batch.item.Chunk
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@DisplayName("리셋 스텝 리스너 단위 테스트")
class ResetStepListenerUnitTest {
    private val questLogRepository: QuestLogRepository = mock<QuestLogRepository>()
    private val questIndexRepository: QuestIndexRepository = mock<QuestIndexRepository>()

    @DisplayName("afterWrite 동작 시")
    @Nested
    inner class TestAfterWrite {
        @DisplayName("항상 jobParameter의 어제 날짜가 loggedDate로 사용된다")
        @Test
        fun `항상 jobParameter의 어제 날짜가 loggedDate로 사용된다`() {
            //given
            val resetDateTimeStr1 = "2022-12-01 05:59:00"
            val resetDateTimeStr2 = "2022-12-01 06:00:00"
            val resetDateTimeStr3 = "2022-12-01 06:01:00"
            val loggedDate = LocalDate.parse(resetDateTimeStr1, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                .minusDays(1)

            val resetStepListener1 = ResetStepListener(questLogRepository, questIndexRepository, resetDateTimeStr1)
            val resetStepListener2 = ResetStepListener(questLogRepository, questIndexRepository, resetDateTimeStr2)
            val resetStepListener3 = ResetStepListener(questLogRepository, questIndexRepository, resetDateTimeStr3)

            val questLogListArgumentCaptor = argumentCaptor<MutableList<QuestLog>>()

            //when
            resetStepListener1.afterWrite(Chunk<Quest>(Mockito.mock(Quest::class.java, Answers.RETURNS_DEEP_STUBS)))
            resetStepListener2.afterWrite(Chunk<Quest>(Mockito.mock(Quest::class.java, Answers.RETURNS_DEEP_STUBS)))
            resetStepListener3.afterWrite(Chunk<Quest>(Mockito.mock(Quest::class.java, Answers.RETURNS_DEEP_STUBS)))

            //then
            verify(questLogRepository, times(3)).saveAll(questLogListArgumentCaptor.capture())

            for (questLogList in questLogListArgumentCaptor.allValues) {
                assertThat(questLogList).allMatch { it.loggedDate == loggedDate }
            }
        }
    }
}