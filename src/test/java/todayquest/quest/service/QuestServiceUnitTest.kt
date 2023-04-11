package todayquest.quest.service

import jakarta.persistence.EntityNotFoundException
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.*
import org.mockito.Mockito.mockStatic
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.security.access.AccessDeniedException
import todayquest.common.MessageUtil
import todayquest.quest.dto.DetailInteractRequest
import todayquest.quest.dto.QuestRequest
import todayquest.quest.entity.*
import todayquest.quest.repository.QuestRepository
import todayquest.user.entity.ProviderType
import todayquest.user.entity.UserInfo
import todayquest.user.repository.UserRepository
import todayquest.user.service.UserService
import java.util.*

@ExtendWith(MockitoExtension::class)
@DisplayName("퀘스트 서비스 유닛 테스트")
class QuestServiceUnitTest {

    @InjectMocks
    lateinit var questService: QuestService

    @Mock
    lateinit var questRepository: QuestRepository

    @Mock
    lateinit var userRepository: UserRepository

    @Mock
    lateinit var userService: UserService

    @Mock
    lateinit var questLogService: QuestLogService

    private lateinit var messageUtil: MockedStatic<MessageUtil>

    private lateinit var questList: List<Quest>
    lateinit var userInfo: UserInfo
    lateinit var quest: Quest

    @BeforeEach
    fun beforeEach() {
        userInfo = UserInfo("", "", ProviderType.GOOGLE)
        quest = Quest("t1", "", userInfo, 1L, QuestState.PROCEED, QuestType.MAIN)
        questList = listOf(
            quest,
            Quest("t2", "", userInfo, 2L, QuestState.PROCEED, QuestType.MAIN),
            Quest("t3", "", userInfo, 3L, QuestState.PROCEED, QuestType.MAIN),
        )

        messageUtil = mockStatic(MessageUtil::class.java)
        `when`(MessageUtil.getMessage(any())).thenReturn("")
        `when`(MessageUtil.getMessage(any(), any())).thenReturn("")
    }

    @AfterEach
    fun afterEach() {
        messageUtil.close()
    }


    @DisplayName("퀘스트 목록 조회 시")
    @Nested
    inner class QuestListTest {

        @DisplayName("요청 파라미터가 제대로 전달된다")
        @Test
        fun `요청 파라미터가 제대로 전달된다`() {
            //given
            val userId = 1L
            val state = QuestState.PROCEED
            val pageNo = 3
            val pageable = PageRequest.of(pageNo, 9)
            val pageList = PageImpl(questList, pageable, questList.size.toLong())
            doReturn(pageList).`when`(questRepository).getQuestsList(eq(userId), eq(state), eq(pageable))

            //when
            val result = questService.getQuestList(userId, state, pageable)

            //then
            assertThat(result.number).isEqualTo(pageNo)
            assertThat(result.content).allMatch { qr -> questList.any {q -> q.title == qr.title } }
        }

    }

    @DisplayName("퀘스트 조회 시")
    @Nested
    inner class GetQuestTest {

        @DisplayName("퀘스트 ID에 해당하는 퀘스트가 없으면 EntityNotFound 예외를 던진다")
        @Test
        fun `퀘스트 조회 결과가 없다면 예외가 발생한다`() {
            //given
            val questId = 0L
            val userId = userInfo.id
            doReturn(Optional.ofNullable(null)).`when`(questRepository).findById(eq(questId))

            //when
            val call = { questService.getQuestInfo(questId, userId) }

            //then
            assertThrows<EntityNotFoundException> { call() }
        }

        @DisplayName("타인의 퀘스트 조회 시 AccessDenied 예외를 던진다")
        @Test
        fun `타인의 퀘스트 조회 테스트`() {
            //given
            val questId = 0L
            val userId = 1L
            doReturn(Optional.of(quest)).`when`(questRepository).findById(eq(questId))

            //when
            val call = { questService.getQuestInfo(questId, userId) }

            //then
            assertThrows<AccessDeniedException> { call() }
        }


        @DisplayName("정상 호출일 경우 퀘스트 조회 로직이 호출된다")
        @Test
        fun `본인의 퀘스트 조회 시 정상 호출된다`() {
            //given
            val questId = 0L
            val userId = userInfo.id
            doReturn(Optional.of(quest)).`when`(questRepository).findById(eq(questId))

            //when
            val questInfo = questService.getQuestInfo(questId, userId)

            //then
            assertThat(questInfo).isNotNull
            assertThat(questInfo.title).isEqualTo(quest.title)
        }

    }

    @DisplayName("퀘스트 저장 시")
    @Nested
    inner class QuestSaveTest {

        @DisplayName("현재 시간이 유저의 코어타임 이라면 dto를 메인 퀘스트로 변경한다")
        @Test
        fun `코어타임이면 퀘스트 타입 메인으로 변경`() {
            //given
            val mockDto = mock<QuestRequest>()
            val mockUser = mock<UserInfo>()
            val mockQuest = mock<Quest>()
            val userId = userInfo.id
            val nextSeq = 1L

            doReturn(true).`when`(mockUser).isNowCoreTime()
            doReturn(mockUser).`when`(userRepository).getReferenceById(eq(userId))
            doReturn(nextSeq).`when`(questRepository).getNextSeqByUserId(eq(userId))
            doReturn(mockQuest).`when`(mockDto).mapToEntity(eq(nextSeq), eq(mockUser))

            //when
            questService.saveQuest(mockDto, userId)

            //then
            verify(mockDto, times(1)).checkRangeOfDeadLine(anyOrNull())
            verify(mockUser, times(1)).isNowCoreTime()
            verify(mockDto, times(1)).toMainQuest()
            verify(questRepository, times(1)).getNextSeqByUserId(eq(userId))
            verify(questRepository, times(1)).save(mockQuest)
            verify(mockQuest, times(1)).updateDetailQuests(any())
            verify(questLogService, times(1)).saveQuestLog(mockQuest)
        }


        @DisplayName("현재 시간이 유저의 코어타임 아니라면 dto를 변경하지 않는다")
        @Test
        fun `코어타임이 아니면 퀘스트 타입 변경 안함`() {
            //given
            val mockDto = mock<QuestRequest>()
            val mockUser = mock<UserInfo>()
            val mockQuest = mock<Quest>()
            val userId = userInfo.id
            val nextSeq = 1L

            doReturn(false).`when`(mockUser).isNowCoreTime()
            doReturn(mockUser).`when`(userRepository).getReferenceById(eq(userId))
            doReturn(nextSeq).`when`(questRepository).getNextSeqByUserId(eq(userId))
            doReturn(mockQuest).`when`(mockDto).mapToEntity(eq(nextSeq), eq(mockUser))

            //when
            questService.saveQuest(mockDto, userId)

            //then
            verify(mockDto, times(1)).checkRangeOfDeadLine(anyOrNull())
            verify(mockUser, times(1)).isNowCoreTime()
            verify(mockDto, times(0)).toMainQuest()
            verify(questRepository, times(1)).getNextSeqByUserId(eq(userId))
            verify(questRepository, times(1)).save(mockQuest)
            verify(mockQuest, times(1)).updateDetailQuests(any())
            verify(questLogService, times(1)).saveQuestLog(mockQuest)
        }


        @DisplayName("정상 호출일 경우 퀘스트 저장 로직이 호출된다")
        @Test
        fun `정상 호출일 경우 퀘스트 저장 로직이 호출된다`() {
            //given
            val mockDto = Mockito.mock(QuestRequest::class.java)
            val userId = userInfo.id
            val nextSeq = 5L
            val mockUser = Mockito.mock(UserInfo::class.java)
            val mockQuest = Mockito.mock(Quest::class.java)

            doReturn(mockUser).`when`(userRepository).getReferenceById(eq(userId))
            doReturn(nextSeq).`when`(questRepository).getNextSeqByUserId(eq(userId))
            doReturn(mockQuest).`when`(mockDto).mapToEntity(eq(nextSeq), eq(mockUser))
            doReturn(quest).`when`(questRepository).save(eq(mockQuest))

            //when
            questService.saveQuest(mockDto, userId)

            //then
            then(mockQuest).should().updateDetailQuests(any())
            then(questLogService).should().saveQuestLog(eq(quest))
        }

    }

    @DisplayName("퀘스트 수정 시")
    @Nested
    inner class QuestUpdateTest {

        @DisplayName("퀘스트 ID에 해당하는 퀘스트가 없으면 EntityNotFound 예외를 던진다")
        @Test
        fun `퀘스트 조회 결과가 없다면 예외가 발생한다`() {
            //given
            val request = QuestRequest("update", "update")
            val questId = 0L
            val userId = userInfo.id
            doReturn(Optional.ofNullable(null)).`when`(questRepository).findById(eq(questId))

            //when
            val call = { questService.updateQuest(request, questId, userId) }

            //then
            assertThrows<EntityNotFoundException> { call() }
        }

        @DisplayName("타인의 퀘스트 요청 시 AccessDenied 예외를 던진다")
        @Test
        fun `타인의 퀘스트 요청 시 오류가 발생한다`() {
            //given
            val request = QuestRequest("update", "update")
            val questId = 0L
            val userId = 1L
            doReturn(Optional.of(quest)).`when`(questRepository).findById(eq(questId))

            //when
            val call = { questService.updateQuest(request, questId, userId) }

            //then
            assertThrows<AccessDeniedException> { call() }
        }


        @DisplayName("진행중인 퀘스트가 아니라면 IllegalState 예외를 던진다")
        @Test
        fun `진행중인 퀘스트가 아니라면 오류가 발생한다`() {
            //given
            val request = QuestRequest("update", "update")
            val questId = 0L
            val userId = userInfo.id

            doReturn(Optional.of(quest)).`when`(questRepository).findById(eq(questId))
            quest.failQuest()

            //when
            val call = { questService.updateQuest(request, questId, userId) }

            //then
            assertThrows<IllegalStateException> { call() }
        }

        @DisplayName("기존 퀘스트가 메인 퀘스트라면 dto의 타입을 변경한다")
        @Test
        fun `메인 퀘스트라면 dto의 타입을 변경한다`() {
            //given
            val mockDto = mock<QuestRequest>()
            val mockQuest = mock<Quest>()
            val mockUser = mock<UserInfo>()

            val questId = 0L
            val userId = userInfo.id

            doReturn(mockUser).`when`(mockQuest).user
            doReturn(Optional.of(mockQuest)).`when`(questRepository).findById(eq(questId))
            doReturn(true).`when`(mockQuest).isMainQuest()

            //when
            questService.updateQuest(mockDto, questId, userId)

            //then
            verify(mockDto, times(1)).checkRangeOfDeadLine(anyOrNull())
            verify(mockQuest, times(1)).checkOwnershipOrThrow(eq(userId))
            verify(mockQuest, times(1)).checkStateIsProceedOrThrow()
            verify(mockQuest, times(1)).isMainQuest()
            verify(mockDto, times(1)).toMainQuest()
            verify(mockQuest, times(1)).updateQuestEntity(mockDto)
        }

        @DisplayName("기존 퀘스트가 서브 퀘스트라면 dto의 타입을 변경하지 않는다")
        @Test
        fun `메인 퀘스트가 아니면 dto의 타입을 변경하지 않는다`() {
            //given
            val mockDto = mock<QuestRequest>()
            val mockQuest = mock<Quest>()
            val mockUser = mock<UserInfo>()

            val questId = 0L
            val userId = userInfo.id

            doReturn(mockUser).`when`(mockQuest).user
            doReturn(Optional.of(mockQuest)).`when`(questRepository).findById(eq(questId))
            doReturn(false).`when`(mockQuest).isMainQuest()

            //when
            questService.updateQuest(mockDto, questId, userId)

            //then
            verify(mockDto, times(1)).checkRangeOfDeadLine(anyOrNull())
            verify(mockQuest, times(1)).checkOwnershipOrThrow(eq(userId))
            verify(mockQuest, times(1)).checkStateIsProceedOrThrow()
            verify(mockQuest, times(1)).isMainQuest()
            verify(mockDto, times(0)).toMainQuest()
            verify(mockQuest, times(1)).updateQuestEntity(mockDto)
        }

        @DisplayName("정상 호출일 경우 퀘스트 업데이트 로직이 호출된다")
        @Test
        fun `퀘스트 업데이트 로직이 호출된다`() {
            //given
            val mockDto = mock<QuestRequest>()
            val mockQuest = mock<Quest>()
            val mockUser = mock<UserInfo>()

            val questId = 0L
            val userId = userInfo.id

            doReturn(mockUser).`when`(mockQuest).user
            doReturn(Optional.of(mockQuest)).`when`(questRepository).findById(eq(questId))

            //when
            questService.updateQuest(mockDto, questId, userId)

            //then
            verify(mockDto, times(1)).checkRangeOfDeadLine(anyOrNull())
            verify(mockQuest, times(1)).checkOwnershipOrThrow(eq(userId))
            verify(mockQuest, times(1)).checkStateIsProceedOrThrow()
            verify(mockQuest, times(1)).isMainQuest()
            verify(mockQuest, times(1)).updateQuestEntity(mockDto)
        }
    }

    @DisplayName("퀘스트 삭제 시")
    @Nested
    inner class QuestDeleteTest {
        @DisplayName("퀘스트 ID에 해당하는 퀘스트가 없으면 EntityNotFound 예외를 던진다")
        @Test
        fun `퀘스트 조회 결과가 없다면 예외가 발생한다`() {
            //given
            val questId = 0L
            val userId = userInfo.id
            doReturn(Optional.ofNullable(null)).`when`(questRepository).findById(eq(questId))

            //when
            val call = { questService.deleteQuest(questId, userId) }

            //then
            assertThrows<EntityNotFoundException> { call() }
        }

        @DisplayName("타인의 퀘스트 요청 시 AccessDenied 예외를 던진다")
        @Test
        fun `타인의 퀘스트 요청 시 오류가 발생한다`() {
            //given
            val questId = 0L
            val userId = 1L
            doReturn(Optional.of(quest)).`when`(questRepository).findById(eq(questId))

            //when
            val call = { questService.deleteQuest(questId, userId) }

            //then
            assertThrows<AccessDeniedException> { call() }
        }

        @DisplayName("정상 호출일 경우 퀘스트 삭제 로직이 호출된다")
        @Test
        fun `퀘스트 삭제 로직이 호출된다`() {
            //given
            val questId = 0L
            val userId = userInfo.id
            val mockQuest = Mockito.mock(Quest::class.java)

            doReturn(Optional.of(mockQuest)).`when`(questRepository).findById(eq(questId))
            doNothing().`when`(mockQuest).checkOwnershipOrThrow(any())

            //when
            questService.deleteQuest(questId, userId)

            //then
            then(mockQuest).should().deleteQuest()
        }
    }

    @DisplayName("퀘스트 완료 시")
    @Nested
    inner class QuestCompleteTest {
        @DisplayName("퀘스트 ID에 해당하는 퀘스트가 없으면 EntityNotFound 예외를 던진다")
        @Test
        fun `퀘스트 조회 결과가 없다면 예외가 발생한다`() {
            //given
            val questId = 0L
            val userId = userInfo.id
            doReturn(Optional.ofNullable(null)).`when`(questRepository).findById(eq(questId))

            //when
            val call = { questService.completeQuest(questId, userId) }

            //then
            assertThrows<EntityNotFoundException> { call() }
        }

        @DisplayName("타인의 퀘스트 요청 시 AccessDenied 예외를 던진다")
        @Test
        fun `타인의 퀘스트 요청 시 오류가 발생한다`() {
            //given
            val questId = 0L
            val userId = 1L
            doReturn(Optional.of(quest)).`when`(questRepository).findById(eq(questId))

            //when
            val call = { questService.completeQuest(questId, userId) }

            //then
            assertThrows<AccessDeniedException> { call() }
        }

        @DisplayName("퀘스트가 삭제된 상태면 IllegalState 예외를 던진다")
        @Test
        fun `퀘스트가 삭제된 상태면 IllegalState 예외 발생`() {
            //given
            val questId = 0L
            val userId = userInfo.id
            val quest = Quest("", "", userInfo, 1L, QuestState.DELETE, QuestType.SUB)
            doReturn(Optional.of(quest)).`when`(questRepository).findById(eq(questId))

            //when
            val call = { questService.completeQuest(questId, userId) }

            //then
            assertThrows<IllegalStateException> { call() }
        }

        @DisplayName("퀘스트가 진행 상태가 아니면 IllegalState 예외를 던진다")
        @Test
        fun `퀘스트가 진행 상태가 아니면 IllegalState 예외 발생`() {
            //given
            val questId = 0L
            val userId = userInfo.id
            val quest = Quest("", "", userInfo, 1L, QuestState.DISCARD, QuestType.SUB)
            doReturn(Optional.of(quest)).`when`(questRepository).findById(eq(questId))

            //when
            val call = { questService.completeQuest(questId, userId) }

            //then
            assertThrows<IllegalStateException> { call() }
        }

        @DisplayName("세부 퀘스트가 모두 완료되지 않았다면 IllegalState 예외를 던진다")
        @Test
        fun `퀘스트 완료가 불가능한 상태면 IllegalState 예외 발생`() {
            //given
            val questId = 0L
            val userId = userInfo.id
            val quest = Quest("", "", userInfo, 1L, QuestState.PROCEED, QuestType.SUB)

            val detailQuests = Quest::class.java.getDeclaredField("_detailQuests")
            detailQuests.isAccessible = true

            val details = mutableListOf(DetailQuest("init1", 1, DetailQuestType.CHECK, DetailQuestState.PROCEED, quest))
            detailQuests.set(quest, details)

            doReturn(Optional.of(quest)).`when`(questRepository).findById(eq(questId))

            //when
            val call = { questService.completeQuest(questId, userId) }

            //then
            assertThrows<IllegalStateException> { call() }
        }

        @DisplayName("정상 호출일 경우 퀘스트 완료 로직이 호출된다")
        @Test
        fun `퀘스트 완료 로직이 호출된다`() {
            //given
            val questId = 0L
            val userId = userInfo.id
            val mockQuest = Mockito.mock(Quest::class.java)
            val mockUser = Mockito.mock(UserInfo::class.java)

            doReturn(Optional.of(mockQuest)).`when`(questRepository).findById(eq(questId))
            doNothing().`when`(mockQuest).checkOwnershipOrThrow(any())
            doReturn(mockUser).`when`(mockQuest).user

            //when
            questService.completeQuest(questId, userId)

            //then
            then(mockQuest).should().completeQuest()
            then(userService).should().earnExpAndGold(anyOrNull(), any())
            then(questLogService).should().saveQuestLog(eq(mockQuest))
        }
    }


    @DisplayName("퀘스트 포기 시")
    @Nested
    inner class QuestDiscardTest {

        @DisplayName("퀘스트 ID에 해당하는 퀘스트가 없으면 EntityNotFound 예외를 던진다")
        @Test
        fun `퀘스트 조회 결과가 없다면 예외가 발생한다`() {
            //given
            val questId = 0L
            val userId = userInfo.id
            doReturn(Optional.ofNullable(null)).`when`(questRepository).findById(eq(questId))

            //when
            val call = { questService.discardQuest(questId, userId) }

            //then
            assertThrows<EntityNotFoundException> { call() }
        }

        @DisplayName("타인의 퀘스트 요청 시 AccessDenied 예외를 던진다")
        @Test
        fun `타인의 퀘스트 요청 시 오류가 발생한다`() {
            //given
            val questId = 0L
            val userId = 1L
            doReturn(Optional.of(quest)).`when`(questRepository).findById(eq(questId))

            //when
            val call = { questService.discardQuest(questId, userId) }

            //then
            assertThrows<AccessDeniedException> { call() }
        }

        @DisplayName("퀘스트가 삭제된 상태면 IllegalState 예외를 던진다")
        @Test
        fun `퀘스트가 삭제된 상태면 IllegalState 예외 발생`() {
            //given
            val questId = 0L
            val userId = userInfo.id
            val quest = Quest("", "", userInfo, 1L, QuestState.DELETE, QuestType.SUB)
            doReturn(Optional.of(quest)).`when`(questRepository).findById(eq(questId))

            //when
            val call = { questService.discardQuest(questId, userId) }

            //then
            assertThrows<IllegalStateException> { call() }
        }

        @DisplayName("퀘스트가 진행 상태가 아니면 IllegalState 예외를 던진다")
        @Test
        fun `퀘스트가 진행 상태가 아니면 IllegalState 예외 발생`() {
            //given
            val questId = 0L
            val userId = userInfo.id
            val quest = Quest("", "", userInfo, 1L, QuestState.DISCARD, QuestType.SUB)
            doReturn(Optional.of(quest)).`when`(questRepository).findById(eq(questId))

            //when
            val call = { questService.discardQuest(questId, userId) }

            //then
            assertThrows<IllegalStateException> { call() }
        }


        @DisplayName("정상 호출일 경우 퀘스트 포기 로직이 호출된다")
        @Test
        fun `퀘스트 포기 로직이 호출된다`() {
            //given
            val questId = 0L
            val userId = userInfo.id
            val mockQuest = Mockito.mock(Quest::class.java)

            doReturn(Optional.of(mockQuest)).`when`(questRepository).findById(eq(questId))
            doNothing().`when`(mockQuest).checkOwnershipOrThrow(any())

            //when
            questService.discardQuest(questId, userId)

            //then
            then(mockQuest).should().discardQuest()
            then(questLogService).should().saveQuestLog(eq(mockQuest))
        }
    }

    @DisplayName("세부 퀘스트 상호 작용 시")
    @Nested
    inner class DetailQuestInteractTest {

        @DisplayName("퀘스트 ID에 해당하는 퀘스트가 없으면 EntityNotFound 예외를 던진다")
        @Test
        fun `퀘스트 조회 결과가 없다면 예외가 발생한다`() {
            //given
            val questId = 0L
            val userId = userInfo.id
            doReturn(Optional.ofNullable(null)).`when`(questRepository).findById(eq(questId))

            //when
            val call = { questService.interactWithDetailQuest(userId, questId, 1L, DetailInteractRequest()) }

            //then
            assertThrows<EntityNotFoundException> { call() }
        }

        @DisplayName("타인의 퀘스트 요청 시 AccessDenied 예외를 던진다")
        @Test
        fun `타인의 퀘스트 요청 시 오류가 발생한다`() {
            //given
            val questId = 0L
            val userId = 1L
            doReturn(Optional.of(quest)).`when`(questRepository).findById(eq(questId))

            //when
            val call = { questService.interactWithDetailQuest(userId, questId, 1L, DetailInteractRequest()) }

            //then
            assertThrows<AccessDeniedException> { call() }
        }

        @DisplayName("정상 호출일 경우 세부 퀘스트 상호 작용 로직이 호출된다")
        @Test
        fun `세부 퀘스트 상호 작용 로직이 호출된다`() {
            //given
            val questId = 0L
            val userId = userInfo.id
            val detailQuestId = 0L
            val mockQuest = Mockito.mock(Quest::class.java)

            doReturn(Optional.of(mockQuest)).`when`(questRepository).findById(eq(questId))
            doNothing().`when`(mockQuest).checkOwnershipOrThrow(any())

            //when
            questService.interactWithDetailQuest(userId, questId, detailQuestId, DetailInteractRequest())

            //then
            then(mockQuest).should().interactWithDetailQuest(eq(detailQuestId), any())
        }
    }


}