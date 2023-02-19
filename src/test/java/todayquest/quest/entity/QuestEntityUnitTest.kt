package todayquest.quest.entity

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.MockedStatic
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
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

        @DisplayName("신규 세부 퀘스트가 추가된다")
        @Test
        fun `신규 세부 퀘스트가 추가된다`() {
            //given
            val quest = Quest("init", "init", userInfo, 1L, QuestState.PROCEED, QuestType.MAIN)
            val detailQuests = Quest::class.java.getDeclaredField("_detailQuests")
            detailQuests.isAccessible = true

            val details = mutableListOf(DetailQuest("init1", 1, DetailQuestType.CHECK, DetailQuestState.PROCEED, quest))
            detailQuests.set(quest, details)

            val detailRequests = listOf(
                DetailRequest("d1", DetailQuestType.CHECK, 1),
                DetailRequest("d2", DetailQuestType.CHECK, 1),
                DetailRequest("d3", DetailQuestType.CHECK, 1),
            )

            //when
            quest.updateDetailQuests(detailRequests)

            //then
            assertThat(quest.detailQuests.size).isEqualTo(detailRequests.size)
            assertThat(quest.detailQuests).allMatch { detail -> detailRequests.any{ request -> detail.title == request.title } }
        }

        @DisplayName("기존 세부 퀘스트가 수정된다")
        @Test
        fun `기존 세부 퀘스트가 수정된다`() {
            //given
            val quest = Quest("init", "init", userInfo, 1L, QuestState.PROCEED, QuestType.MAIN)

            val detailQuests = Quest::class.java.getDeclaredField("_detailQuests")
            detailQuests.isAccessible = true

            val details = mutableListOf(
                DetailQuest("init1", 1, DetailQuestType.CHECK, DetailQuestState.PROCEED, quest),
                DetailQuest("init2", 1, DetailQuestType.CHECK, DetailQuestState.PROCEED, quest),
                DetailQuest("init3", 1, DetailQuestType.CHECK, DetailQuestState.PROCEED, quest),
            )
            detailQuests.set(quest, details)

            val detailRequests = listOf(
                DetailRequest("d1", DetailQuestType.CHECK, 1),
                DetailRequest("d2", DetailQuestType.CHECK, 1),
                DetailRequest("d3", DetailQuestType.CHECK, 1),
            )

            //when
            quest.updateDetailQuests(detailRequests)

            //then
            assertThat(details).allMatch { detail -> detailRequests.any { dto -> detail.title == dto.title } }
        }

        @DisplayName("기존 세부 퀘스트가 더 많다면 삭제된다")
        @Test
        fun `기존 세부 퀘스트가 더 많다면 삭제된다`() {
            val quest = Quest("init", "init", userInfo, 1L, QuestState.PROCEED, QuestType.MAIN)

            val detailQuests = Quest::class.java.getDeclaredField("_detailQuests")
            detailQuests.isAccessible = true

            val details = mutableListOf(
                DetailQuest("init1", 1, DetailQuestType.CHECK, DetailQuestState.PROCEED, quest),
                DetailQuest("init2", 1, DetailQuestType.CHECK, DetailQuestState.PROCEED, quest),
                DetailQuest("init3", 1, DetailQuestType.CHECK, DetailQuestState.PROCEED, quest),
            )
            detailQuests.set(quest, details)

            val detailRequests = listOf(
                DetailRequest("d1", DetailQuestType.CHECK, 1),
            )

            //when
            quest.updateDetailQuests(detailRequests)

            //then
            assertThat(details.size).isEqualTo(detailRequests.size)
        }
    }

    @DisplayName("퀘스트 완료 시")
    @Nested
    inner class QuestCompleteTest {

        @DisplayName("퀘스트가 삭제된 상태면 IllegalArgument 예외를 던진다")
        @Test
        fun `퀘스트가 삭제된 상태면 IllegalArgument 예외 발생`() {
            //given
            val quest = Quest("", "", userInfo, 1L, QuestState.DELETE, QuestType.SUB)

            //when
            val call = { quest.completeQuest() }

            //then
            assertThrows<IllegalArgumentException> { call() }
        }

        @DisplayName("퀘스트가 진행 상태가 아니면 IllegalArgument 예외를 던진다")
        @Test
        fun `퀘스트가 진행 상태가 아니면 IllegalArgument 예외 발생`() {
            //given
            val quest = Quest("", "", userInfo, 1L, QuestState.DISCARD, QuestType.SUB)

            //when
            val call = { quest.completeQuest() }

            //then
            assertThrows<IllegalArgumentException> { call() }
        }

        @DisplayName("세부 퀘스트가 모두 완료되지 않았다면 IllegalArgument 예외를 던진다")
        @Test
        fun `퀘스트 완료가 불가능한 상태면 IllegalArgument 예외 발생`() {
            //given
            val quest = Quest("", "", userInfo, 1L, QuestState.PROCEED, QuestType.SUB)
            val detailQuests = Quest::class.java.getDeclaredField("_detailQuests")
            detailQuests.isAccessible = true

            val details = mutableListOf(DetailQuest("init1", 1, DetailQuestType.CHECK, DetailQuestState.PROCEED, quest))
            detailQuests.set(quest, details)

            //when
            val call = { quest.completeQuest() }

            //then
            assertThrows<IllegalArgumentException> { call() }
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

        @DisplayName("퀘스트가 삭제된 상태면 IllegalArgument 예외를 던진다")
        @Test
        fun `퀘스트가 삭제된 상태면 IllegalArgument 예외 발생`() {
            //given
            val quest = Quest("", "", userInfo, 1L, QuestState.DELETE, QuestType.SUB)

            //when
            val call = { quest.discardQuest() }

            //then
            assertThrows<IllegalArgumentException> { call() }
        }

        @DisplayName("퀘스트가 진행 상태가 아니면 IllegalArgument 예외를 던진다")
        @Test
        fun `퀘스트가 진행 상태가 아니면 IllegalArgument 예외 발생`() {
            //given
            val quest = Quest("", "", userInfo, 1L, QuestState.DISCARD, QuestType.SUB)

            //when
            val call = { quest.discardQuest() }

            //then
            assertThrows<IllegalArgumentException> { call() }
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

        @DisplayName("퀘스트가 진행 상태가 아니면 IllegalArgument 예외를 던진다")
        @Test
        fun `퀘스트가 진행 상태가 아니면 IllegalArgument 예외를 던진다`() {
            //given
            val quest = Quest("", "", userInfo, 1L, QuestState.DISCARD, QuestType.SUB)

            //when
            val call = { quest.checkIsProceedingQuest() }

            //then
            assertThrows<IllegalArgumentException>(call)
        }

        @DisplayName("퀘스트가 진행 상태면 오류를 던지지 않는다")
        @Test
        fun `퀘스트가 진행 상태면 오류를 던지지 않는다`() {
            //given
            val quest = Quest("", "", userInfo, 1L, QuestState.PROCEED, QuestType.SUB)

            //when
            val call = { quest.checkIsProceedingQuest() }

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
            val call = { quest.checkIsQuestOfValidUser(1L) }

            //then
            assertThrows<AccessDeniedException>(call)
        }


        @DisplayName("퀘스트 소유자와 요청자 ID가 같은 경우 정상 호출된다")
        @Test
        fun `퀘스트 소유자와 요청자 ID가 같은 경우 정상 호출된다`() {
            //given
            val quest = Quest("", "", userInfo, 1L, QuestState.DISCARD, QuestType.SUB)

            //when
            val call = { quest.checkIsQuestOfValidUser(userInfo.id) }

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

            val details = mutableListOf(DetailQuest("init1", 1, DetailQuestType.CHECK, DetailQuestState.COMPLETE, quest))
            detailQuests.set(quest, details)

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

            val details = mutableListOf(DetailQuest("init1", 1, DetailQuestType.CHECK, DetailQuestState.PROCEED, quest))
            detailQuests.set(quest, details)

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

        @DisplayName("ID가 일치하는 세부 퀘스트가 없다면 IllegalArgument 예외를 던진다")
        @Test
        fun `ID가 일치하는 세부 퀘스트가 없다면 IllegalArgument 예외를 던진다`() {
            //given
            val quest = Quest("", "", userInfo, 1L, QuestState.PROCEED, QuestType.MAIN)
            val detailQuests = Quest::class.java.getDeclaredField("_detailQuests")
            detailQuests.isAccessible = true

            val details = mutableListOf(DetailQuest("init1", 1, DetailQuestType.CHECK, DetailQuestState.COMPLETE, quest))
            detailQuests.set(quest, details)

            //when
            val call = { quest.interactWithDetailQuest(1) }

            //then
            assertThrows<IllegalArgumentException> { call() }
        }

        @DisplayName("퀘스트가 진행 상태가 아니라면 IllegalArgument 예외를 던진다")
        @Test
        fun `퀘스트가 진행 상태가 아니라면 IllegalArgument 예외를 던진다`() {
            //given
            val quest = Quest("", "", userInfo, 1L, QuestState.FAIL, QuestType.MAIN)
            val detailQuests = Quest::class.java.getDeclaredField("_detailQuests")
            detailQuests.isAccessible = true

            val details = mutableListOf(DetailQuest("init1", 1, DetailQuestType.CHECK, DetailQuestState.COMPLETE, quest))
            detailQuests.set(quest, details)

            //when
            val call = { quest.interactWithDetailQuest(0) }

            //then
            assertThrows<IllegalArgumentException> { call() }
        }

        @DisplayName("REQUEST가 NULL이 아니면 REQUEST의 COUNT로 변경한다")
        @Test
        fun `REQUEST가 NULL이 아니면 REQUEST의 COUNT로 변경한다`() {
            //given
            val quest = Quest("", "", userInfo, 1L, QuestState.PROCEED, QuestType.MAIN)
            val interactRequest = DetailInteractRequest(3)
            val detailQuests = Quest::class.java.getDeclaredField("_detailQuests")
            detailQuests.isAccessible = true

            val details = mutableListOf(DetailQuest("init1", 5, DetailQuestType.COUNT, DetailQuestState.PROCEED, quest))
            detailQuests.set(quest, details)

            //when
            val interactResult = quest.interactWithDetailQuest(0, interactRequest)

            //then
            assertThat(interactResult.count).isEqualTo(interactRequest.count)
            assertThat(details[0].count).isEqualTo(interactRequest.count)
        }

        @DisplayName("세부 퀘스트가 완료 상태면 카운트를 리셋하고 진행 상태로 변경한다")
        @Test
        fun `세부 퀘스트가 완료 상태면 카운트를 리셋하고 진행 상태로 변경한다`() {
            //given
            val quest = Quest("", "", userInfo, 1L, QuestState.PROCEED, QuestType.MAIN)
            val detailQuests = Quest::class.java.getDeclaredField("_detailQuests")
            detailQuests.isAccessible = true

            val targetCount = 5
            val detail = DetailQuest("init1", targetCount, DetailQuestType.COUNT, DetailQuestState.COMPLETE, quest)
            detail.changeCount(targetCount)

            val beforeCount = detail.count

            val details = mutableListOf(detail)
            detailQuests.set(quest, details)

            //when
            val interactResult = quest.interactWithDetailQuest(0)

            //then
            assertThat(beforeCount).isNotEqualTo(0)
            assertThat(interactResult.count).isEqualTo(0)
            assertThat(interactResult.state).isEqualTo(DetailQuestState.PROCEED)
            assertThat(details[0].count).isEqualTo(0)
            assertThat(details[0].state).isEqualTo(DetailQuestState.PROCEED)
        }


        @DisplayName("다른 경우에 해당하지 않으면 카운트를 1 증가시킨다")
        @Test
        fun `다른 경우에 해당하지 않으면 카운트를 1 증가시킨다`() {
            //given
            val quest = Quest("", "", userInfo, 1L, QuestState.PROCEED, QuestType.MAIN)
            val detailQuests = Quest::class.java.getDeclaredField("_detailQuests")
            detailQuests.isAccessible = true

            val targetCount = 5
            val detail = DetailQuest("init1", targetCount, DetailQuestType.COUNT, DetailQuestState.PROCEED, quest)

            val beforeCount = detail.count

            val details = mutableListOf(detail)
            detailQuests.set(quest, details)

            //when
            val interactResult = quest.interactWithDetailQuest(0)

            //then
            val afterCount = beforeCount + 1
            assertThat(interactResult.count).isEqualTo(afterCount)
            assertThat(details[0].count).isEqualTo(afterCount)
        }
    }

}