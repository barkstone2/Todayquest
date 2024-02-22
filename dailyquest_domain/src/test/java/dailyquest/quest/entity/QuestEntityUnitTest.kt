package dailyquest.quest.entity

import dailyquest.user.entity.ProviderType
import dailyquest.user.entity.UserInfo
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
@DisplayName("퀘스트 엔티티 유닛 테스트")
class QuestEntityUnitTest {

    private lateinit var userInfo: UserInfo
    private lateinit var quest: Quest

    @BeforeEach
    fun beforeEach() {
        userInfo = UserInfo("", "", ProviderType.GOOGLE)
        quest = Quest("t1", "", userInfo, 1L, QuestState.PROCEED, QuestType.MAIN)
    }

    @DisplayName("엔티티 수정 메서드 호출 시")
    @Nested
    inner class EntityUpdateTest {
        @DisplayName("details 인자가 null이면 emptyList를 updateDetailQuest 메서드에 전달한다")
        @Test
        fun `details 인자가 null이면 emptyList를 updateDetailQuest 메서드에 전달한다`() {
            //given
            val quest = Quest("init", "init", userInfo, 1L, QuestState.PROCEED, QuestType.MAIN)
            val title = "update"
            val description = "update"
            val details = listOf(Pair(1L, mock<DetailQuest>()))
            quest.updateDetailQuests(details)

            //when
            quest.updateQuestEntity(title, description, null, null)

            //then
            assertThat(quest.detailQuests).isEmpty()
        }

        @DisplayName("details 인자가 null이 아니면 입력 인자를 updateDetailQuest 메서드에 전달한다")
        @Test
        fun `details 인자가 null이 아니면 입력 인자를 updateDetailQuest 메서드에 전달한다`() {
            //given
            val quest = Quest("init", "init", userInfo, 1L, QuestState.PROCEED, QuestType.MAIN)
            val title = "update"
            val description = "update"
            val details = listOf(Pair(1L, mock<DetailQuest>()))

            //when
            quest.updateQuestEntity(title, description, null, details)

            //then
            assertThat(quest.detailQuests.size).isEqualTo(details.size)
            assertThat(quest.detailQuests[0]).isEqualTo(details[0].second)
        }

        @DisplayName("넘겨 받은 인자로 엔티티를 업데이트 한다")
        @Test
        fun `넘겨 받은 인자로 엔티티를 업데이트 한다`() {
            //given
            val quest = Quest("init", "init", userInfo, 1L, QuestState.PROCEED, QuestType.MAIN)
            val title = "update"
            val description = "update"
            val deadLine = LocalDateTime.of(2022, 12, 12, 12, 0, 0)

            //when
            quest.updateQuestEntity(title, description, deadLine, null)

            //then
            assertThat(quest.title).isEqualTo(title)
            assertThat(quest.description).isEqualTo(description)
            assertThat(quest.deadLine).isEqualTo(deadLine)
        }
    }

    @Nested
    @DisplayName("세부 퀘스트 수정 시")
    inner class DetailQuestUpdateTest {

        @DisplayName("신규 세부 퀘스트가 더 많다면, 기존 리스트 크기만큼은 업데이트하고, 초과한 부분은 새로 추가한다")
        @Test
        fun `신규 세부 퀘스트가 더 많다면, 기존 리스트 크기만큼은 업데이트하고, 초과한 부분은 새로 추가한다`() {
            //given
            val quest = Quest("init", "init", userInfo, 1L, QuestState.PROCEED, QuestType.MAIN)

            val detailQuests = Quest::class.java.getDeclaredField("_detailQuests")
            detailQuests.isAccessible = true

            val mockDetail = Mockito.mock(DetailQuest::class.java)
            val details = Mockito.mock(MutableList::class.java)

            detailQuests.set(quest, details)

            val detailRequests = listOf(
                Pair(1L, DetailQuest.of("d1", 1, DetailQuestType.CHECK, DetailQuestState.PROCEED, quest)),
                Pair(2L, DetailQuest.of("d2", 1, DetailQuestType.CHECK, DetailQuestState.PROCEED, quest)),
                Pair(3L, DetailQuest.of("d3", 1, DetailQuestType.CHECK, DetailQuestState.PROCEED, quest)),
            )

            val currentDetailsSize = 1
            doReturn(mockDetail).`when`(details)[argThat { i -> i < currentDetailsSize }]
            doThrow(ArrayIndexOutOfBoundsException::class).`when`(details)[argThat { i -> i >= currentDetailsSize }]
            doReturn(currentDetailsSize).`when`(details).size

            //when
            quest.updateDetailQuests(detailRequests)

            //then
            verify(mockDetail, times(currentDetailsSize)).updateDetailQuest(any(), any())
            verify(details).addAll(argThat { list -> list.size == detailRequests.size - currentDetailsSize})
            verify(details, never()).removeAt(any())
        }

        @DisplayName("기존 리스트와 새로운 리스트 크기가 같다면, 리스트 업데이트만 발생한다")
        @Test
        fun `기존 리스트와 새로운 리스트 크기가 같다면, 리스트 업데이트만 발생한다`() {
            //given
            val quest = Quest("init", "init", userInfo, 1L, QuestState.PROCEED, QuestType.MAIN)

            val detailQuests = Quest::class.java.getDeclaredField("_detailQuests")
            detailQuests.isAccessible = true

            val mockDetail = Mockito.mock(DetailQuest::class.java)

            val details = Mockito.mock(MutableList::class.java)

            detailQuests.set(quest, details)

            val detailRequests = listOf(
                Pair(1L, DetailQuest.of("d1", 1, DetailQuestType.CHECK, DetailQuestState.PROCEED, quest)),
                Pair(2L, DetailQuest.of("d2", 1, DetailQuestType.CHECK, DetailQuestState.PROCEED, quest)),
                Pair(3L, DetailQuest.of("d3", 1, DetailQuestType.CHECK, DetailQuestState.PROCEED, quest)),
            )

            doReturn(mockDetail).`when`(details)[any()]
            doReturn(detailRequests.size).`when`(details).size

            //when
            quest.updateDetailQuests(detailRequests)

            //then
            verify(mockDetail, times(detailRequests.size)).updateDetailQuest(any(), any())
            verify(details, never()).removeAt(any())
            verify(details).addAll(argThat { list -> list.isEmpty() })
        }

        @DisplayName("기존 세부 퀘스트가 더 많다면 오버된 퀘스트는 삭제된다")
        @Test
        fun `기존 세부 퀘스트가 더 많다면 오버된 퀘스트는 삭제된다`() {
            //given
            val quest = Quest("init", "init", userInfo, 1L, QuestState.PROCEED, QuestType.MAIN)

            val detailQuests = Quest::class.java.getDeclaredField("_detailQuests")
            detailQuests.isAccessible = true

            val mockDetail = Mockito.mock(DetailQuest::class.java)

            val details = Mockito.mock(MutableList::class.java)

            detailQuests.set(quest, details)

            val detailRequests = listOf(
                Pair(1L, DetailQuest.of("d1", 1, DetailQuestType.CHECK, DetailQuestState.PROCEED, quest)),
                Pair(2L, DetailQuest.of("d2", 1, DetailQuestType.CHECK, DetailQuestState.PROCEED, quest)),
                Pair(3L, DetailQuest.of("d3", 1, DetailQuestType.CHECK, DetailQuestState.PROCEED, quest)),
            )

            val currentDetailsSize = 5
            doReturn(mockDetail).`when`(details)[any()]
            doReturn(currentDetailsSize).`when`(details).size

            //when
            quest.updateDetailQuests(detailRequests)

            //then
            verify(mockDetail, times(detailRequests.size)).updateDetailQuest(any(), any())
            verify(details, times(currentDetailsSize - detailRequests.size)).removeAt(any())
            verify(details).addAll(argThat { list -> list.isEmpty() })
        }
    }

    @DisplayName("퀘스트 완료 시")
    @Nested
    inner class QuestCompleteTest {

        @DisplayName("퀘스트가 진행 상태가 아니면 상태 변경 없이 현재 상태를 반환한다")
        @Test
        fun `퀘스트가 진행 상태가 아니면 상태 변경 없이 현재 상태를 반환한다`() {
            //given
            val quest = Quest("", "", userInfo, 1L, QuestState.DISCARD, QuestType.SUB)

            //when
            val resultState = quest.completeQuest()

            //then
            assertThat(resultState).isEqualTo(QuestState.DISCARD)
        }

        @DisplayName("세부 퀘스트가 모두 완료되지 않았다면, 상태 변경 없이 현재 상태가 반환된다")
        @Test
        fun `세부 퀘스트가 모두 완료되지 않았다면, 상태 변경 없이 현재 상태가 반환된다`() {
            //given
            val quest = Quest("", "", userInfo, 1L, QuestState.PROCEED, QuestType.SUB)
            val detailQuests = Quest::class.java.getDeclaredField("_detailQuests")
            detailQuests.isAccessible = true

            val mockDetail = Mockito.mock(DetailQuest::class.java)
            val details = mutableListOf(mockDetail)
            detailQuests.set(quest, details)

            doReturn(false).`when`(mockDetail).isCompleted()

            //when
            val resultState = quest.completeQuest()

            //then
            assertThat(resultState).isEqualTo(QuestState.PROCEED)
        }

        @DisplayName("현재 상태가 진행 상태이면서 모든 세부 퀘스트가 완료 상태라면, 퀘스트를 완료 상태로 변경 후 변경된 상태를 반환한다")
        @Test
        fun `현재 상태가 진행 상태이면서 모든 세부 퀘스트가 완료 상태라면, 퀘스트를 완료 상태로 변경 후 변경된 상태를 반환한다`() {
            //given
            val quest = Quest("", "", userInfo, 1L, QuestState.PROCEED, QuestType.SUB)
            val detailQuests = Quest::class.java.getDeclaredField("_detailQuests")
            detailQuests.isAccessible = true

            val mockDetail = Mockito.mock(DetailQuest::class.java)
            val details = mutableListOf(mockDetail)
            detailQuests.set(quest, details)

            doReturn(true).`when`(mockDetail).isCompleted()

            //when
            val resultState = quest.completeQuest()

            //then
            assertThat(resultState).isEqualTo(QuestState.COMPLETE)
        }

        @DisplayName("퀘스트 완료가 가능한 상태면 완료 상태로 변경된다")
        @Test
        fun `퀘스트 완료가 가능한 상태면 완료 상태로 변경된다`() {
            //given
            val quest = Quest("", "", userInfo, 1L, QuestState.PROCEED, QuestType.SUB)

            //when
            quest.completeQuest()

            //then
            assertThat(quest.state).isEqualTo(QuestState.COMPLETE)
        }

    }

    @DisplayName("퀘스트 삭제 시 삭제 상태로 변경된다")
    @Test
    fun `퀘스트 삭제 시 삭제 상태로 변경된다`() {
        //given
        val quest = Quest("", "", userInfo, 1L, QuestState.PROCEED, QuestType.SUB)

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
            val quest = Quest("", "", userInfo, 1L, QuestState.DISCARD, QuestType.SUB)

            //when
            val resultState = quest.discardQuest()

            //then
            assertThat(resultState).isEqualTo(QuestState.DISCARD)
        }

        @DisplayName("퀘스트가 진행 상태라면 상태 변경 후 현재 상태가 반환된다")
        @Test
        fun `퀘스트가 진행 상태라면 상태 변경 후 현재 상태가 반환된다`() {
            //given
            val quest = Quest("", "", userInfo, 1L, QuestState.PROCEED, QuestType.SUB)

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
        val quest = Quest("", "", userInfo, 1L, QuestState.PROCEED, QuestType.SUB)

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
            val quest = Quest("", "", userInfo, 1L, QuestState.DISCARD, QuestType.SUB)

            //when
            val isProceed = quest.isProceed()

            //then
            assertThat(isProceed).isFalse()
        }

        @DisplayName("퀘스트가 진행 상태면 true를 반환한다")
        @Test
        fun `퀘스트가 진행 상태면 true를 반환한다`() {
            //given
            val quest = Quest("", "", userInfo, 1L, QuestState.PROCEED, QuestType.SUB)

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
            val quest = Quest("", "", userInfo, 1L, QuestState.PROCEED, QuestType.SUB)

            //when
            val canComplete = quest.canComplete()

            //then
            assertThat(canComplete).isTrue
        }


        @DisplayName("세부 퀘스트가 모두 완료 상태면 true를 반환한다")
        @Test
        fun `세부 퀘스트가 모두 완료 상태면 true를 반환한다`() {
            //given
            val quest = Quest("", "", userInfo, 1L, QuestState.PROCEED, QuestType.SUB)

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
            val quest = Quest("", "", userInfo, 1L, QuestState.PROCEED, QuestType.SUB)

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
            val quest = Quest("", "", userInfo, 1L, QuestState.PROCEED, QuestType.MAIN)

            //when
            val isMainQuest = quest.isMainQuest()

            //then
            assertThat(isMainQuest).isTrue
        }

        @DisplayName("SUB 타입이면 false를 반환한다")
        @Test
        fun `SUB 타입이면 false를 반환한다`() {
            //given
            val quest = Quest("", "", userInfo, 1L, QuestState.PROCEED, QuestType.SUB)

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
            val quest = Quest("", "", userInfo, 1L, QuestState.PROCEED, QuestType.MAIN)

            //when
            val interactResult = quest.updateDetailQuestCount(1, 1)

            //then
            assertThat(interactResult).isNull()
        }

        @DisplayName("세부 퀘스트가 존재하면 updateCountAndState 메서드가 호출된다")
        @Test
        fun ifDetailFoundThenCallUpdateMethod() {
            //given
            val quest = Quest("", "", userInfo, 1L, QuestState.PROCEED, QuestType.MAIN)
            val count = 3
            val detailQuests = Quest::class.java.getDeclaredField("_detailQuests")
            detailQuests.isAccessible = true

            val mockDetail = Mockito.mock(DetailQuest::class.java)
            val details = mutableListOf(mockDetail)
            detailQuests.set(quest, details)

            //when
            quest.updateDetailQuestCount(0, count)

            //then
            verify(mockDetail, times(1)).updateCountAndState(eq(count))
        }
    }

}