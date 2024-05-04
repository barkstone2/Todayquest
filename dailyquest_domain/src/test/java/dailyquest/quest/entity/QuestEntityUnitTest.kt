
package dailyquest.quest.entity

import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito
import org.mockito.kotlin.*
import java.time.LocalDateTime

@ExtendWith(MockKExtension::class)
@DisplayName("퀘스트 엔티티 유닛 테스트")
class QuestEntityUnitTest {

    private lateinit var quest: Quest

    @BeforeEach
    fun beforeEach() {
        quest = Quest("t1", "", 1L, 1L, QuestState.PROCEED, QuestType.MAIN)
    }

    @DisplayName("엔티티 수정 메서드 호출 시")
    @Nested
    inner class EntityUpdateTest {
        @DisplayName("details 인자가 없으면 emptyList를 replaceDetailQuests 메서드에 전달한다")
        @Test
        fun `details 인자가 없으면 emptyList를 replaceDetailQuests 메서드에 전달한다`() {
            //given
            val quest = Quest("init", "init", 1L, 1L, QuestState.PROCEED, QuestType.MAIN)
            val title = "update"
            val description = "update"
            val details = listOf(mock<DetailQuest>())
            quest.replaceDetailQuests(details)

            //when
            quest.updateQuestEntity(title, description, null)

            //then
            assertThat(quest.detailQuests).isEmpty()
        }

        @DisplayName("details 인자가 null이 아니면 입력 인자를 replaceDetailQuests 메서드에 전달한다")
        @Test
        fun `details 인자가 null이 아니면 입력 인자를 replaceDetailQuests 메서드에 전달한다`() {
            //given
            val quest = Quest("init", "init", 1L, 1L, QuestState.PROCEED, QuestType.MAIN)
            val title = "update"
            val description = "update"
            val details = listOf(mock<DetailQuest>())

            //when
            quest.updateQuestEntity(title, description, null, details)

            //then
            assertThat(quest.detailQuests.size).isEqualTo(details.size)
            assertThat(quest.detailQuests[0]).isEqualTo(details[0])
        }

        @DisplayName("넘겨 받은 인자로 엔티티를 업데이트 한다")
        @Test
        fun `넘겨 받은 인자로 엔티티를 업데이트 한다`() {
            //given
            val quest = Quest("init", "init", 1L, 1L, QuestState.PROCEED, QuestType.MAIN)
            val title = "update"
            val description = "update"
            val deadLine = LocalDateTime.of(2022, 12, 12, 12, 0, 0)

            //when
            quest.updateQuestEntity(title, description, deadLine)

            //then
            assertThat(quest.title).isEqualTo(title)
            assertThat(quest.description).isEqualTo(description)
            assertThat(quest.deadLine).isEqualTo(deadLine)
        }
    }

    @DisplayName("퀘스트 완료 시")
    @Nested
    inner class QuestCompleteTest {

        @DisplayName("퀘스트가 진행 상태가 아니면 퀘스트 상태가 완료로 변경되지 않는다")
        @Test
        fun `퀘스트가 진행 상태가 아니면 퀘스트 상태가 완료로 변경되지 않는다`() {
            //given
            val quest = Quest("", "", 1L, 1L, QuestState.DISCARD, QuestType.SUB)

            //when
            quest.completeQuestIfPossible()

            //then
            assertThat(quest.state).isNotEqualTo(QuestState.COMPLETE)
        }

        @DisplayName("퀘스트가 진행 상태지만 세부 퀘스트가 모두 완료되지 않았다면, 퀘스트 상태가 완료로 변경되지 않는다")
        @Test
        fun `퀘스트가 진행 상태지만 세부 퀘스트가 모두 완료되지 않았다면, 퀘스트 상태가 완료로 변경되지 않는다`() {
            //given
            val state = QuestState.PROCEED
            val quest = Quest("", "", 1L, 1L, state, QuestType.SUB)
            val mockDetail = mockk<DetailQuest>()
            val details = mutableListOf(mockDetail)
            quest.replaceDetailQuests(details)
            every { mockDetail.isCompleted() } returns false

            //when
            quest.completeQuestIfPossible()

            //then
            assertThat(quest.state).isNotEqualTo(QuestState.COMPLETE)
        }

        @DisplayName("퀘스트가 진행 상태이면서 모든 세부 퀘스트가 완료 상태라면, 퀘스트를 완료 상태로 변경한다")
        @Test
        fun `퀘스트가 진행 상태이면서 모든 세부 퀘스트가 완료 상태라면, 퀘스트를 완료 상태로 변경한다`() {
            //given
            val state = QuestState.PROCEED
            val quest = Quest("", "", 1L, 1L, state, QuestType.SUB)
            val mockDetail = mockk<DetailQuest>()
            val details = mutableListOf(mockDetail)
            quest.replaceDetailQuests(details)
            every { mockDetail.isCompleted() } returns true

            //when
            quest.completeQuestIfPossible()

            //then
            assertThat(quest.state).isEqualTo(QuestState.COMPLETE)
        }
    }

    @DisplayName("퀘스트 삭제 시 삭제 상태로 변경된다")
    @Test
    fun `퀘스트 삭제 시 삭제 상태로 변경된다`() {
        //given
        val quest = Quest("", "", 1L, 1L, QuestState.PROCEED, QuestType.SUB)

        //when
        quest.deleteQuest()

        //then
        assertThat(quest.state).isEqualTo(QuestState.DELETE)
    }

    @Nested
    @DisplayName("퀘스트 포기 시")
    inner class QuestDiscardTest {

        @DisplayName("퀘스트가 진행 상태가 아니면 상태 변경 없이 현재 상태가 반환된다")
        @Test
        fun `퀘스트가 진행 상태가 아니면 상태 변경 없이 현재 상태가 반환된다`() {
            //given
            val quest = Quest("", "", 1L, 1L, QuestState.DISCARD, QuestType.SUB)

            //when
            val resultState = quest.discardQuest()

            //then
            assertThat(resultState).isEqualTo(QuestState.DISCARD)
        }

        @DisplayName("퀘스트가 진행 상태라면 상태 변경 후 현재 상태가 반환된다")
        @Test
        fun `퀘스트가 진행 상태라면 상태 변경 후 현재 상태가 반환된다`() {
            //given
            val quest = Quest("", "", 1L, 1L, QuestState.PROCEED, QuestType.SUB)

            //when
            val resultState = quest.discardQuest()

            //then
            assertThat(resultState).isEqualTo(QuestState.DISCARD)
        }
    }


    @DisplayName("퀘스트 실패 메서드 호출 시 실패 상태로 변경된다")
    @Test
    fun `퀘스트 실패 메서드 호출 시 실패 상태로 변경된다`() {
        //given
        val quest = Quest("", "", 1L, 1L, QuestState.PROCEED, QuestType.SUB)

        //when
        quest.failQuest()

        //then
        assertThat(quest.state).isEqualTo(QuestState.FAIL)
    }

    @DisplayName("퀘스트 진행 상태 체크 메서드 테스트")
    @Nested
    inner class QuestProceedingCheckTest {

        @DisplayName("퀘스트가 진행 상태가 아니면 false 를 반환한다")
        @Test
        fun `퀘스트가 진행 상태가 아니면 false 를 반환한다`() {
            //given
            val quest = Quest("", "", 1L, 1L, QuestState.DISCARD, QuestType.SUB)

            //when
            val isProceed = quest.isProceed()

            //then
            assertThat(isProceed).isFalse()
        }

        @DisplayName("퀘스트가 진행 상태면 true를 반환한다")
        @Test
        fun `퀘스트가 진행 상태면 true를 반환한다`() {
            //given
            val quest = Quest("", "", 1L, 1L, QuestState.PROCEED, QuestType.SUB)

            //when
            val isProceed = quest.isProceed()

            //then
            assertThat(isProceed).isTrue()
        }
    }

    @Nested
    @DisplayName("세부 퀘스트 전체 완료 학인 테스트")
    inner class CanCompleteTest {

        @DisplayName("등록된 세부 퀘스트가 없다면 true를 반환한다")
        @Test
        fun `등록된 세부 퀘스트가 없다면 true를 반환한다`() {
            //given
            val quest = Quest("", "", 1L, 1L, QuestState.PROCEED, QuestType.SUB)

            //when
            val canComplete = quest.canComplete()

            //then
            assertThat(canComplete).isTrue
        }


        @DisplayName("세부 퀘스트가 모두 완료 상태면 true를 반환한다")
        @Test
        fun `세부 퀘스트가 모두 완료 상태면 true를 반환한다`() {
            //given
            val quest = Quest("", "", 1L, 1L, QuestState.PROCEED, QuestType.SUB)

            val detailQuests = Quest::class.java.getDeclaredField("_detailQuests")
            detailQuests.isAccessible = true

            val mockDetail = Mockito.mock(DetailQuest::class.java)
            val details = mutableListOf(mockDetail)
            detailQuests.set(quest, details)

            doReturn(true).`when`(mockDetail).isCompleted()

            //when
            val canComplete = quest.canComplete()

            //then
            assertThat(canComplete).isTrue
        }

        @DisplayName("세부 퀘스트가 모두 완료 상태가 아니면 false를 반환한다")
        @Test
        fun `세부 퀘스트가 모두 완료 상태가 아니면 false를 반환한다`() {
            //given
            val quest = Quest("", "", 1L, 1L, QuestState.PROCEED, QuestType.SUB)

            val detailQuests = Quest::class.java.getDeclaredField("_detailQuests")
            detailQuests.isAccessible = true

            val mockDetail = Mockito.mock(DetailQuest::class.java)
            val details = mutableListOf(mockDetail)
            detailQuests.set(quest, details)

            doReturn(false).`when`(mockDetail).isCompleted()

            //when
            val canComplete = quest.canComplete()

            //then
            assertThat(canComplete).isFalse
        }
    }

    @DisplayName("isMainQuest 호출 시")
    @Nested
    inner class IsMainQuestTest {

        @DisplayName("MAIN 타입이면 true를 반환한다")
        @Test
        fun `MAIN 타입이면 true를 반환한다`() {
            //given
            val quest = Quest("", "", 1L, 1L, QuestState.PROCEED, QuestType.MAIN)

            //when
            val isMainQuest = quest.isMainQuest()

            //then
            assertThat(isMainQuest).isTrue
        }

        @DisplayName("SUB 타입이면 false를 반환한다")
        @Test
        fun `SUB 타입이면 false를 반환한다`() {
            //given
            val quest = Quest("", "", 1L, 1L, QuestState.PROCEED, QuestType.SUB)

            //when
            val isMainQuest = quest.isMainQuest()

            //then
            assertThat(isMainQuest).isFalse
        }
    }


    @DisplayName("세부 퀘스트 카운트 변경 시")
    @Nested
    inner class TestUpdateDetailQuestCount {

        @DisplayName("ID가 일치하는 세부 퀘스트가 없다면 null이 반환된다")
        @Test
        fun `ID가 일치하는 세부 퀘스트가 없다면 null이 반환된다`() {
            //given
            val quest = Quest("", "", 1L, 1L, QuestState.PROCEED, QuestType.MAIN)

            //when
            val interactResult = quest.updateDetailQuestCount(1, 1)

            //then
            assertThat(interactResult).isNull()
        }

        @DisplayName("세부 퀘스트가 존재하면 updateCountAndState 메서드가 호출된다")
        @Test
        fun ifDetailFoundThenCallUpdateMethod() {
            //given
            val quest = Quest("", "", 1L, 1L, QuestState.PROCEED, QuestType.MAIN)
            val mockDetail = Mockito.mock(DetailQuest::class.java)
            quest.replaceDetailQuests(listOf(mockDetail))

            val count = 3

            //when
            quest.updateDetailQuestCount(0, count)

            //then
            verify(mockDetail, times(1)).updateCountAndState(eq(count))
        }
    }

}