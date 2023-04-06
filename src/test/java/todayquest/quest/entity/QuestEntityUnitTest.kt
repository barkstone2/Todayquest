package todayquest.quest.entity

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.MockedStatic
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import org.springframework.security.access.AccessDeniedException
import todayquest.common.MessageUtil
import todayquest.quest.dto.DetailInteractRequest
import todayquest.quest.dto.DetailRequest
import todayquest.quest.dto.QuestRequest
import todayquest.user.entity.ProviderType
import todayquest.user.entity.UserInfo
import java.util.*

@ExtendWith(MockitoExtension::class)
@DisplayName("퀘스트 엔티티 유닛 테스트")
class QuestEntityUnitTest {

    private lateinit var messageUtil: MockedStatic<MessageUtil>
    private lateinit var userInfo: UserInfo
    lateinit var quest: Quest

    @BeforeEach
    fun beforeEach() {
        userInfo = UserInfo("", "", ProviderType.GOOGLE)
        quest = Quest("t1", "", userInfo, 1L, QuestState.PROCEED, QuestType.MAIN)

        messageUtil = Mockito.mockStatic(MessageUtil::class.java)
        Mockito.`when`(MessageUtil.getMessage(any())).thenReturn("")
        Mockito.`when`(MessageUtil.getMessage(any(), any())).thenReturn("")
    }

    @AfterEach
    fun afterEach() {
        messageUtil.close()
    }

    @DisplayName("엔티티 수정 테스트")
    @Test
    fun `엔티티 수정 테스트`() {
        //given
        val quest = Quest("init", "init", userInfo, 1L, QuestState.PROCEED, QuestType.MAIN)
        val dto = QuestRequest("update", "update")

        //when
        quest.updateQuestEntity(dto)

        //then
        assertThat(quest.title).isEqualTo(dto.title)
        assertThat(quest.description).isEqualTo(dto.description)
    }

    @Nested
    @DisplayName("세부 퀘스트 수정 시")
    inner class DetailQuestUpdateTest {

        @DisplayName("신규 세부 퀘스트가 리스트에 추가된다")
        @Test
        fun `신규 세부 퀘스트가 리스트에 추가된다`() {
            //given
            val quest = Quest("init", "init", userInfo, 1L, QuestState.PROCEED, QuestType.MAIN)

            val detailQuests = Quest::class.java.getDeclaredField("_detailQuests")
            detailQuests.isAccessible = true

            val mockDetail = Mockito.mock(DetailQuest::class.java)

            val details = Mockito.mock(MutableList::class.java)

            detailQuests.set(quest, details)

            val detailRequests = listOf(
                DetailRequest("d1", DetailQuestType.CHECK, 1),
                DetailRequest("d2", DetailQuestType.CHECK, 1),
                DetailRequest("d3", DetailQuestType.CHECK, 1),
            )

            val currentDetailsSize = 1
            doReturn(mockDetail).`when`(details)[argThat { i -> i < currentDetailsSize }]
            doThrow(ArrayIndexOutOfBoundsException::class).`when`(details)[argThat { i -> i >= currentDetailsSize }]
            doReturn(currentDetailsSize).`when`(details).size

            //when
            quest.updateDetailQuests(detailRequests)

            //then
            verify(mockDetail, times(currentDetailsSize)).updateDetailQuest(any())
            verify(details, never()).removeAt(any())
            verify(details).addAll(argThat { list -> list.size == detailRequests.size - currentDetailsSize})
        }

        @DisplayName("기존 세부 퀘스트가 수정된다")
        @Test
        fun `기존 세부 퀘스트가 수정된다`() {
            //given
            val quest = Quest("init", "init", userInfo, 1L, QuestState.PROCEED, QuestType.MAIN)

            val detailQuests = Quest::class.java.getDeclaredField("_detailQuests")
            detailQuests.isAccessible = true

            val mockDetail = Mockito.mock(DetailQuest::class.java)

            val details = Mockito.mock(MutableList::class.java)

            detailQuests.set(quest, details)

            val detailRequests = listOf(
                DetailRequest("d1", DetailQuestType.CHECK, 1),
                DetailRequest("d2", DetailQuestType.CHECK, 1),
                DetailRequest("d3", DetailQuestType.CHECK, 1),
            )

            doReturn(mockDetail).`when`(details)[any()]
            doReturn(detailRequests.size).`when`(details).size

            //when
            quest.updateDetailQuests(detailRequests)

            //then
            verify(mockDetail, times(detailRequests.size)).updateDetailQuest(any())
            verify(details, never()).removeAt(any())
            verify(details).addAll(argThat { list -> list.isEmpty() })
        }

        @DisplayName("기존 세부 퀘스트가 더 많다면 오버된 세부 퀘스트는 삭제된다")
        @Test
        fun `기존 세부 퀘스트가 더 많다면 오버된 세부 퀘스트는 삭제된다`() {
            //given
            val quest = Quest("init", "init", userInfo, 1L, QuestState.PROCEED, QuestType.MAIN)

            val detailQuests = Quest::class.java.getDeclaredField("_detailQuests")
            detailQuests.isAccessible = true

            val mockDetail = Mockito.mock(DetailQuest::class.java)

            val details = Mockito.mock(MutableList::class.java)

            detailQuests.set(quest, details)

            val detailRequests = listOf(
                DetailRequest("d1", DetailQuestType.CHECK, 1),
                DetailRequest("d2", DetailQuestType.CHECK, 1),
                DetailRequest("d3", DetailQuestType.CHECK, 1),
            )

            val currentDetailsSize = 5
            doReturn(mockDetail).`when`(details)[any()]
            doReturn(currentDetailsSize).`when`(details).size

            //when
            quest.updateDetailQuests(detailRequests)

            //then
            verify(mockDetail, times(detailRequests.size)).updateDetailQuest(any())
            verify(details, times(currentDetailsSize - detailRequests.size)).removeAt(any())
            verify(details).addAll(argThat { list -> list.isEmpty() })
        }
    }

    @DisplayName("퀘스트 완료 시")
    @Nested
    inner class QuestCompleteTest {

        @DisplayName("퀘스트가 삭제된 상태면 IllegalState 예외를 던진다")
        @Test
        fun `퀘스트가 삭제된 상태면 IllegalState 예외 발생`() {
            //given
            val quest = Quest("", "", userInfo, 1L, QuestState.DELETE, QuestType.SUB)

            //when
            val call = { quest.completeQuest() }

            //then
            assertThrows<IllegalStateException> { call() }
        }

        @DisplayName("퀘스트가 진행 상태가 아니면 IllegalState 예외를 던진다")
        @Test
        fun `퀘스트가 진행 상태가 아니면 IllegalState 예외 발생`() {
            //given
            val quest = Quest("", "", userInfo, 1L, QuestState.DISCARD, QuestType.SUB)

            //when
            val call = { quest.completeQuest() }

            //then
            assertThrows<IllegalStateException> { call() }
        }

        @DisplayName("세부 퀘스트가 모두 완료되지 않았다면 IllegalState 예외를 던진다")
        @Test
        fun `세부 퀘스트가 모두 완료되지 않았다면 IllegalState 예외 발생`() {
            //given
            val quest = Quest("", "", userInfo, 1L, QuestState.PROCEED, QuestType.SUB)
            val detailQuests = Quest::class.java.getDeclaredField("_detailQuests")
            detailQuests.isAccessible = true

            val mockDetail = Mockito.mock(DetailQuest::class.java)
            val details = mutableListOf(mockDetail)
            detailQuests.set(quest, details)

            doReturn(false).`when`(mockDetail).isCompletedDetailQuest()

            //when
            val call = { quest.completeQuest() }

            //then
            assertThrows<IllegalStateException> { call() }
        }

        @DisplayName("세부 퀘스트가 모두 완료됐다면 완료 상태로 변경된다")
        @Test
        fun `세부 퀘스트가 모두 완료됐다면 완료 상태로 변경된다`() {
            //given
            val quest = Quest("", "", userInfo, 1L, QuestState.PROCEED, QuestType.SUB)
            val detailQuests = Quest::class.java.getDeclaredField("_detailQuests")
            detailQuests.isAccessible = true

            val mockDetail = Mockito.mock(DetailQuest::class.java)
            val details = mutableListOf(mockDetail)
            detailQuests.set(quest, details)

            doReturn(true).`when`(mockDetail).isCompletedDetailQuest()

            //when
            quest.completeQuest()

            //then
            assertThat(quest.state).isEqualTo(QuestState.COMPLETE)

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

        @DisplayName("퀘스트가 삭제된 상태면 IllegalState 예외를 던진다")
        @Test
        fun `퀘스트가 삭제된 상태면 IllegalState 예외 발생`() {
            //given
            val quest = Quest("", "", userInfo, 1L, QuestState.DELETE, QuestType.SUB)

            //when
            val call = { quest.discardQuest() }

            //then
            assertThrows<IllegalStateException> { call() }
        }

        @DisplayName("퀘스트가 진행 상태가 아니면 IllegalState 예외를 던진다")
        @Test
        fun `퀘스트가 진행 상태가 아니면 IllegalState 예외 발생`() {
            //given
            val quest = Quest("", "", userInfo, 1L, QuestState.DISCARD, QuestType.SUB)

            //when
            val call = { quest.discardQuest() }

            //then
            assertThrows<IllegalStateException> { call() }
        }


        @DisplayName("퀘스트 포기가 가능한 상태면 포기 상태로 변경된다")
        @Test
        fun `퀘스트 포기가 가능한 상태면 포기 상태로 변경된다`() {
            //given
            val quest = Quest("", "", userInfo, 1L, QuestState.PROCEED, QuestType.SUB)

            //when
            quest.discardQuest()

            //then
            assertThat(quest.state).isEqualTo(QuestState.DISCARD)
        }
    }


    @DisplayName("퀘스트 포기 시 포기 상태로 변경된다")
    @Test
    fun `퀘스트 포기 시 포기 상태로 변경된다`() {
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

        @DisplayName("퀘스트가 진행 상태가 아니면 IllegalState 예외를 던진다")
        @Test
        fun `퀘스트가 진행 상태가 아니면 IllegalState 예외를 던진다`() {
            //given
            val quest = Quest("", "", userInfo, 1L, QuestState.DISCARD, QuestType.SUB)

            //when
            val call = { quest.checkStateIsProceedOrThrow() }

            //then
            assertThrows<IllegalStateException>(call)
        }

        @DisplayName("퀘스트가 진행 상태면 오류를 던지지 않는다")
        @Test
        fun `퀘스트가 진행 상태면 오류를 던지지 않는다`() {
            //given
            val quest = Quest("", "", userInfo, 1L, QuestState.PROCEED, QuestType.SUB)

            //when
            val call = { quest.checkStateIsProceedOrThrow() }

            //then
            assertDoesNotThrow(call)
        }
    }


    @DisplayName("퀘스트 소유자 체크 메서드 테스트")
    @Nested
    inner class QuestOwnerTest {

        @DisplayName("퀘스트 소유자와 요청자 ID가 다른 경우 AccessDenied 예외를 던진다")
        @Test
        fun `퀘스트 소유자와 요청자 ID가 다른 경우 AccessDenied 예외를 던진다`() {
            //given
            val quest = Quest("", "", userInfo, 1L, QuestState.DISCARD, QuestType.SUB)

            //when
            val call = { quest.checkOwnershipOrThrow(1L) }

            //then
            assertThrows<AccessDeniedException>(call)
        }


        @DisplayName("퀘스트 소유자와 요청자 ID가 같은 경우 정상 호출된다")
        @Test
        fun `퀘스트 소유자와 요청자 ID가 같은 경우 정상 호출된다`() {
            //given
            val quest = Quest("", "", userInfo, 1L, QuestState.DISCARD, QuestType.SUB)

            //when
            val call = { quest.checkOwnershipOrThrow(userInfo.id) }

            //then
            assertDoesNotThrow(call)
        }
    }

    @Nested
    @DisplayName("세부 퀘스트 전체 확인 완료 테스트")
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

            doReturn(true).`when`(mockDetail).isCompletedDetailQuest()

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

            doReturn(false).`when`(mockDetail).isCompletedDetailQuest()

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


    @DisplayName("세부 퀘스트 상호 작용 시")
    @Nested
    inner class InteractWithDetailQuestTest {

        @DisplayName("ID가 일치하는 세부 퀘스트가 없다면 IllegalState 예외를 던진다")
        @Test
        fun `ID가 일치하는 세부 퀘스트가 없다면 IllegalState 예외를 던진다`() {
            //given
            val quest = Quest("", "", userInfo, 1L, QuestState.PROCEED, QuestType.MAIN)

            //when
            val call = { quest.interactWithDetailQuest(1) }

            //then
            assertThrows<IllegalStateException> { call() }
        }

        @DisplayName("퀘스트가 진행 상태가 아니라면 IllegalState 예외를 던진다")
        @Test
        fun `퀘스트가 진행 상태가 아니라면 IllegalState 예외를 던진다`() {
            //given
            val quest = Quest("", "", userInfo, 1L, QuestState.FAIL, QuestType.MAIN)

            //when
            val call = { quest.interactWithDetailQuest(0) }

            //then
            assertThrows<IllegalStateException> { call() }
        }

        @DisplayName("REQUEST가 NULL이 아니면 changeCount가 호출된다")
        @Test
        fun `REQUEST가 NULL이 아니면 changeCount가 호출된다`() {
            //given
            val quest = Quest("", "", userInfo, 1L, QuestState.PROCEED, QuestType.MAIN)
            val interactRequest = DetailInteractRequest(3)
            val detailQuests = Quest::class.java.getDeclaredField("_detailQuests")
            detailQuests.isAccessible = true

            val mockDetail = Mockito.mock(DetailQuest::class.java)
            val details = mutableListOf(mockDetail)
            detailQuests.set(quest, details)

            //when
            quest.interactWithDetailQuest(0, interactRequest)

            //then
            verify(mockDetail, times(1)).changeCount(any())
            verify(mockDetail, never()).resetCount()
            verify(mockDetail, never()).addCount()
        }

        @DisplayName("세부 퀘스트가 완료 상태면 resetCount가 호출된다")
        @Test
        fun `세부 퀘스트가 완료 상태면 resetCount가 호출된다`() {
            //given
            val quest = Quest("", "", userInfo, 1L, QuestState.PROCEED, QuestType.MAIN)
            val detailQuests = Quest::class.java.getDeclaredField("_detailQuests")
            detailQuests.isAccessible = true

            val mockDetail = Mockito.mock(DetailQuest::class.java)
            val details = mutableListOf(mockDetail)
            detailQuests.set(quest, details)

            doReturn(true).`when`(mockDetail).isCompletedDetailQuest()

            //when
            quest.interactWithDetailQuest(0)

            //then
            verify(mockDetail, never()).changeCount(any())
            verify(mockDetail, times(1)).resetCount()
            verify(mockDetail, never()).addCount()
        }


        @DisplayName("다른 경우에 해당하지 않으면 addCount가 호출된다")
        @Test
        fun `다른 경우에 해당하지 않으면 addCount가 호출된다`() {
            //given
            val quest = Quest("", "", userInfo, 1L, QuestState.PROCEED, QuestType.MAIN)
            val detailQuests = Quest::class.java.getDeclaredField("_detailQuests")
            detailQuests.isAccessible = true

            val mockDetail = Mockito.mock(DetailQuest::class.java)
            val details = mutableListOf(mockDetail)
            detailQuests.set(quest, details)

            //when
            quest.interactWithDetailQuest(0)

            //then
            verify(mockDetail, never()).changeCount(any())
            verify(mockDetail, never()).resetCount()
            verify(mockDetail, times(1)).addCount()
        }
    }

}