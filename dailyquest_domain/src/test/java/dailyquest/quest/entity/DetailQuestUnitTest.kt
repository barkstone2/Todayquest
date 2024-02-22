package dailyquest.quest.entity

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock

@DisplayName("세부 퀘스트 유닛 테스트")
class DetailQuestUnitTest {

    @DisplayName("updateCountAndState 호출 시")
    @Nested
    inner class TestUpdateCountAndState {
        private val mockQuest = mock<Quest>()

        @DisplayName("요청 count가 null이고 현재 완료 상태라면 현재 카운트가 0이 된다")
        @Test
        fun `요청 count가 null이고 현재 완료 상태라면 현재 카운트가 0이 된다`() {
            //given
            val currentState = DetailQuestState.COMPLETE
            val initialCount = 1
            val targetDetailQuest = DetailQuest.of("proceed", 10,
                initialCount, DetailQuestType.COUNT, currentState, mockQuest)

            //when
            val resultDetailQuest = targetDetailQuest.updateCountAndState(null)

            //then
            assertThat(resultDetailQuest.count).isEqualTo(0)
        }

        @DisplayName("요청 count가 null이고 현재 완료 상태가 아니면 현재 카운트가 1 증가한다")
        @Test
        fun `요청 count가 null이고 현재 완료 상태가 아니면 현재 카운트가 1 증가한다`() {
            //given
            val currentState = DetailQuestState.PROCEED
            val initialCount = 1
            val increasedCount = initialCount + 1
            val targetDetailQuest = DetailQuest.of("title", 10, initialCount, DetailQuestType.COUNT, currentState, mock<Quest>())

            //when
            val resultDetailQuest = targetDetailQuest.updateCountAndState(null)

            //then
            assertThat(resultDetailQuest.count).isEqualTo(increasedCount)
        }
        
        @DisplayName("요청 count가 null이 아니고 targetCount보다 작다면, 현재 카운트가 요청 count가 된다")
        @Test
        fun `요청 count가 null이 아니고 targetCount보다 작다면, 현재 카운트가 요청 count가 된다`() {
            //given
            val targetCount = 10
            val initialCount = 1
            val requestCount = 5
            val targetDetailQuest = DetailQuest.of("title", targetCount,
                initialCount, DetailQuestType.COUNT, DetailQuestState.PROCEED, mock<Quest>())

            //when
            val resultDetailQuest = targetDetailQuest.updateCountAndState(requestCount)

            //then
            assertThat(resultDetailQuest.count).isEqualTo(requestCount)
        }

        @DisplayName("요청 count가 null이 아니고 targetCount보다 작다면, 현재 상태가 PROCEED가 된다")
        @Test
        fun `요청 count가 null이 아니고 targetCount보다 작다면, 현재 상태가 PROCEED가 된다`() {
            //given
            val currentState = DetailQuestState.COMPLETE
            val targetCount = 10
            val requestCount = 5
            val detailQuest = DetailQuest.of("title", targetCount, DetailQuestType.COUNT, currentState, mock<Quest>())

            //when
            val resultDetailQuest = detailQuest.updateCountAndState(requestCount)

            //then
            assertThat(resultDetailQuest.state).isEqualTo(DetailQuestState.PROCEED)
        }
        @DisplayName("요청 count가 null이 아니고 targetCount보다 크다면, 현재 카운트가 targetCount가 된다")
        @Test
        fun `요청 count가 null이 아니고 targetCount보다 크다면, 현재 카운트가 targetCount가 된다`() {
            //given
            val targetCount = 5
            val initialCount = 1
            val requestCount = 10
            val detailQuest = DetailQuest.of("title", targetCount,
                initialCount, DetailQuestType.COUNT, DetailQuestState.COMPLETE, mock<Quest>())

            //when
            val resultDetailQuest = detailQuest.updateCountAndState(requestCount)

            //then
            assertThat(resultDetailQuest.count).isEqualTo(targetCount)
        }
        
        @DisplayName("요청 count가 null이 아니고 targetCount보다 크다면, 현재 상태가 COMPLETE가 된다")
        @Test
        fun `요청 count가 null이 아니고 targetCount보다 크다면, 현재 상태가 COMPLETE가 된다`() {
            //given
            val currentState = DetailQuestState.PROCEED
            val targetCount = 5
            val requestCount = 10
            val detailQuest = DetailQuest.of("title", targetCount, DetailQuestType.COUNT, currentState, mock<Quest>())

            //when
            val resultDetailQuest = detailQuest.updateCountAndState(requestCount)

            //then
            assertThat(resultDetailQuest.state).isEqualTo(DetailQuestState.COMPLETE)
        }
    }

    @DisplayName("resetCount 호출 시")
    @Nested
    inner class TestResetCount {
        @DisplayName("현재 상태가 PROCEED가 된다")
        @Test
        fun `현재 상태가 PROCEED가 된다`() {
            //given
            val targetDetailQuest = DetailQuest.of("title", 10, DetailQuestType.COUNT, DetailQuestState.COMPLETE, mock<Quest>())

            //when
            targetDetailQuest.resetCount()

            //then
            assertThat(targetDetailQuest.state).isEqualTo(DetailQuestState.PROCEED)
        }

        @DisplayName("현재 카운트가 0이 된다")
        @Test
        fun `현재 카운트가 0이 된다`() {
            //given
            val initialCount = 1
            val targetDetailQuest = DetailQuest.of("title", 10,
                initialCount, DetailQuestType.COUNT, DetailQuestState.PROCEED, mock<Quest>())

            //when
            targetDetailQuest.resetCount()

            //then
            assertThat(targetDetailQuest.count).isZero()
        }
    }

    @DisplayName("addCount 호출 시")
    @Nested
    inner class TestAddCount {
        @DisplayName("현재 카운트가 1 증가한다")
        @Test
        fun `현재 카운트가 1 증가한다`() {
            //given
            val initialCount = 1
            val targetDetailQuest = DetailQuest.of("title", 10,
                initialCount, DetailQuestType.COUNT, DetailQuestState.PROCEED, mock<Quest>())
            val increasedCount = initialCount + 1

            //when
            targetDetailQuest.addCount()

            //then
            assertThat(targetDetailQuest.count).isEqualTo(increasedCount)
        }

        @DisplayName("증가된 카운트가 targetCount와 같다면 현재 상태가 COMPLETE가 된다")
        @Test
        fun `증가된 카운트가 targetCount와 같다면 현재 상태가 COMPLETE가 된다`() {
            //given
            val initialCount = 0
            val targetCount = 1
            val targetDetailQuest = DetailQuest.of("title", targetCount,
                initialCount, DetailQuestType.COUNT, DetailQuestState.PROCEED, mock<Quest>())

            //when
            targetDetailQuest.addCount()

            //then
            assertThat(targetDetailQuest.state).isEqualTo(DetailQuestState.COMPLETE)
        }

        @DisplayName("증가된 카운트가 targetCount보다 크다면 변경이 발생하지 않아야 한다")
        @Test
        fun `증가된 카운트가 targetCount보다 크다면 변경이 발생하지 않아야 한다`() {
            //given
            val initialCount = 1
            val targetCount = 1
            val targetDetailQuest = DetailQuest.of("title", targetCount,
                initialCount, DetailQuestType.COUNT, DetailQuestState.PROCEED, mock<Quest>())

            //when
            targetDetailQuest.addCount()

            //then
            assertThat(targetDetailQuest.count).isEqualTo(initialCount)
        }
    }
}