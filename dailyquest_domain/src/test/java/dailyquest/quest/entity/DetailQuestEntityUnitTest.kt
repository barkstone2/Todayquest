package dailyquest.quest.entity

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*

@DisplayName("세부 퀘스트 엔티티 단위 테스트")
class DetailQuestEntityUnitTest {


    @DisplayName("addCount 호출 시")
    @Nested
    inner class AddCountTest {
        @DisplayName("현재 카운트가 1 증가한다")
        @Test
        fun `현재 카운트가 1 증가한다`() {
            //given
            val detailQuest = DetailQuest("", 10, DetailQuestType.COUNT, DetailQuestState.PROCEED, mock<Quest>())
            val beforeCount = detailQuest.count

            //when
            detailQuest.addCount()

            //then
            assertThat(detailQuest.count).isEqualTo(beforeCount+1)
        }

        @DisplayName("목표 count에 도달하면 COMPLETE 상태가 된다")
        @Test
        fun `목표 count에 도달하면 COMPLETE 상태가 된다`() {
            //given
            val detailQuest = DetailQuest("", 1, DetailQuestType.COUNT, DetailQuestState.PROCEED, mock<Quest>())

            //when
            detailQuest.addCount()

            //then
            assertThat(detailQuest.state).isEqualTo(DetailQuestState.COMPLETE)
        }
    }

    @DisplayName("resetCount 호출 시 현재 카운트가 0이 되고 PROCEED 상태가 된다")
    @Test
    fun `resetCount 호출 시 현재 카운트가 0이 되고 PROCEED 상태가 된다`() {
        //given
        val detailQuest = DetailQuest("", 1, DetailQuestType.CHECK, DetailQuestState.COMPLETE, mock<Quest>())
        detailQuest.addCount()
        val beforeCount = detailQuest.count

        //when
        detailQuest.resetCount()

        //then
        assertThat(beforeCount).isNotEqualTo(0)
        assertThat(detailQuest.count).isEqualTo(0)
        assertThat(detailQuest.state).isEqualTo(DetailQuestState.PROCEED)
    }

    @DisplayName("changeCount 호출 시")
    @Nested
    inner class ChangeCountTest {
        @DisplayName("현재 카운트가 인자로 넘어온 값으로 변한다")
        @Test
        fun `현재 카운트가 인자로 넘어온 값으로 변한다`() {
            //given
            val detailQuest = DetailQuest("", 10, DetailQuestType.COUNT, DetailQuestState.PROCEED, mock<Quest>())
            val countParameter = 5

            //when
            detailQuest.changeCount(countParameter)

            //then
            assertThat(detailQuest.count).isEqualTo(countParameter)
        }

        @DisplayName("인자 값이 목표 카운트보다 크면 현재 카운트가 목표 카운트로 변한다")
        @Test
        fun `인자 값이 목표 카운트보다 크면 현재 카운트가 목표 카운트로 변한다`() {
            //given
            val targetCount = 5
            val detailQuest = DetailQuest("", targetCount, DetailQuestType.COUNT, DetailQuestState.PROCEED, mock<Quest>())
            val countParameter = 10

            //when
            detailQuest.changeCount(countParameter)

            //then
            assertThat(detailQuest.count).isEqualTo(targetCount)
        }

        @DisplayName("인자 카운트가 목표 카운트보다 작으면 PROCEED 상태로 변한다")
        @Test
        fun `인자 카운트가 목표 카운트보다 작으면 PROCEED 상태로 변한다`() {
            //given
            val targetCount = 10
            val detailQuest = DetailQuest("", targetCount, DetailQuestType.COUNT, DetailQuestState.COMPLETE, mock<Quest>())
            val countParameter = 5

            //when
            detailQuest.changeCount(countParameter)

            //then
            assertThat(detailQuest.state).isEqualTo(DetailQuestState.PROCEED)
        }

        @DisplayName("인자 카운트가 목표 카운트와 같으면 COMPLETE 상태로 변한다")
        @Test
        fun `인자 카운트가 목표 카운트와 같으면 COMPLETE 상태로 변한다`() {
            //given
            val targetCount = 5
            val detailQuest = DetailQuest("", targetCount, DetailQuestType.COUNT, DetailQuestState.PROCEED, mock<Quest>())
            val countParameter = 5

            //when
            detailQuest.changeCount(countParameter)

            //then
            assertThat(detailQuest.state).isEqualTo(DetailQuestState.COMPLETE)
        }
    }

    @DisplayName("isCompletedDetailQuest 호출 시 현재 상태가 COMPLETE면 true를 반환한다")
    @Test
    fun `isCompletedDetailQuest 호출 시 현재 상태가 COMPLETE면 true를 반환한다`() {
        //given
        val detailQuest = DetailQuest("", 5, DetailQuestType.COUNT, DetailQuestState.COMPLETE, mock<Quest>())

        //when
        val isCompletedDetailQuest = detailQuest.isCompletedDetailQuest()

        //then
        assertThat(isCompletedDetailQuest).isTrue()
    }

    @DisplayName("interact 호출 시")
    @Nested
    inner class InteractTest {
        @DisplayName("count가 null이 아니면 changeCount가 호출된다")
        @Test
        fun `count가 null이 아니면 changeCount가 호출된다`() {
            //given
            val spyDetailQuest = spy<DetailQuest>()

            //when
            spyDetailQuest.interact(5)

            //then
            verify(spyDetailQuest, times(1)).changeCount(any())
        }

        @DisplayName("count가 null이고 현재 완료 상태면 resetCount가 호출된다")
        @Test
        fun `count가 null이고 현재 완료 상태면 resetCount가 호출된다`() {
            //given
            val spyDetailQuest = spy<DetailQuest>()
            doReturn(true).`when`(spyDetailQuest).isCompletedDetailQuest()

            //when
            spyDetailQuest.interact(null)

            //then
            verify(spyDetailQuest, times(0)).changeCount(any())
            verify(spyDetailQuest, times(1)).resetCount()
        }

        @DisplayName("count가 null이고 완료 상태가 아니면 addCount가 호출된다")
        @Test
        fun `count가 null이고 완료 상태가 아니면 addCount가 호출된다`() {
            //given
            val spyDetailQuest = spy<DetailQuest>()
            doReturn(false).`when`(spyDetailQuest).isCompletedDetailQuest()

            //when
            spyDetailQuest.interact(null)

            //then
            verify(spyDetailQuest, times(0)).changeCount(any())
            verify(spyDetailQuest, times(0)).resetCount()
            verify(spyDetailQuest, times(1)).addCount()
        }
    }


}