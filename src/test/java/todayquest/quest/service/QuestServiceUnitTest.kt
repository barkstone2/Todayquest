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
import todayquest.common.MessageUtil
import todayquest.common.RestPage
import todayquest.quest.dto.DetailInteractRequest
import todayquest.quest.dto.QuestRequest
import todayquest.quest.dto.QuestResponse
import todayquest.quest.entity.*
import todayquest.quest.repository.QuestRepository
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

    @BeforeEach
    fun beforeEach() {
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
            val pageSize = 9
            val pageable = PageRequest.of(pageNo, pageSize)
            val list = PageImpl<Quest>(listOf())

            doReturn(list).`when`(questRepository).getQuestsList(
                eq(userId),
                eq(state),
                argThat { page ->
                    page.pageNumber == pageable.pageNumber
                    && page.pageSize == pageable.pageSize
                }
            )

            //when
            val result = questService.getQuestList(userId, state, pageable)

            //then
            verify(questRepository, times(1))
                .getQuestsList(
                    eq(userId),
                    eq(state),
                    argThat { page ->
                        page.pageNumber == pageable.pageNumber
                        && page.pageSize == pageable.pageSize
                    }
                )

            assertThat(result).isInstanceOf(RestPage::class.java)
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
            val userId = 0L
            doReturn(Optional.ofNullable(null)).`when`(questRepository).findById(eq(questId))

            //when
            val call = { questService.getQuestInfo(questId, userId) }

            //then
            assertThrows<EntityNotFoundException> { call() }
        }

        @DisplayName("퀘스트 소유자 확인 메서드가 호출된다")
        @Test
        fun `퀘스트 소유자 확인 메서드가 호출된다`() {
            //given
            val questId = 0L
            val userId = 1L

            val mockQuest = mock<Quest>()
            doReturn(Optional.of(mockQuest)).`when`(questRepository).findById(eq(questId))

            //when
            val questInfo = questService.getQuestInfo(questId, userId)

            //then
            verify(mockQuest, times(1)).checkOwnershipOrThrow(eq(userId))
            assertThat(questInfo).isInstanceOf(QuestResponse::class.java)
        }
    }

    @DisplayName("퀘스트 저장 시")
    @Nested
    inner class QuestSaveTest {

        @DisplayName("현재 시간이 유저의 코어타임이라면 타입 변경 로직을 호출한다")
        @Test
        fun `코어타임이면 퀘스트 타입 메인으로 변경`() {
            //given
            val mockDto = mock<QuestRequest>()
            val mockUser = mock<UserInfo>()
            val mockQuest = mock<Quest>()
            val userId = 0L
            val nextSeq = 1L

            doReturn(true).`when`(mockUser).isNowCoreTime()
            doReturn(mockUser).`when`(userRepository).getReferenceById(eq(userId))
            doReturn(nextSeq).`when`(questRepository).getNextSeqByUserId(eq(userId))
            doReturn(mockQuest).`when`(mockDto).mapToEntity(eq(nextSeq), eq(mockUser))

            //when
            val saveQuest = questService.saveQuest(mockDto, userId)

            //then
            verify(mockDto, times(1)).checkRangeOfDeadLine(anyOrNull())
            verify(mockUser, times(1)).isNowCoreTime()
            verify(mockDto, times(1)).toMainQuest()
            verify(questRepository, times(1)).getNextSeqByUserId(eq(userId))
            verify(questRepository, times(1)).save(mockQuest)
            verify(mockQuest, times(1)).updateDetailQuests(any())
            verify(questLogService, times(1)).saveQuestLog(mockQuest)
            assertThat(saveQuest).isInstanceOf(QuestResponse::class.java)

        }


        @DisplayName("현재 시간이 유저의 코어타임 아니라면 타입 변경 로직을 호출하지 않는다")
        @Test
        fun `코어타임이 아니면 퀘스트 타입 변경 안함`() {
            //given
            val mockDto = mock<QuestRequest>()
            val mockUser = mock<UserInfo>()
            val mockQuest = mock<Quest>()
            val userId = 0L
            val nextSeq = 1L

            doReturn(false).`when`(mockUser).isNowCoreTime()
            doReturn(mockUser).`when`(userRepository).getReferenceById(eq(userId))
            doReturn(nextSeq).`when`(questRepository).getNextSeqByUserId(eq(userId))
            doReturn(mockQuest).`when`(mockDto).mapToEntity(eq(nextSeq), eq(mockUser))

            //when
            val saveQuest = questService.saveQuest(mockDto, userId)

            //then
            verify(mockDto, times(1)).checkRangeOfDeadLine(anyOrNull())
            verify(mockUser, times(1)).isNowCoreTime()
            verify(mockDto, times(0)).toMainQuest()
            verify(questRepository, times(1)).getNextSeqByUserId(eq(userId))
            verify(questRepository, times(1)).save(mockQuest)
            verify(mockQuest, times(1)).updateDetailQuests(any())
            verify(questLogService, times(1)).saveQuestLog(mockQuest)
            assertThat(saveQuest).isInstanceOf(QuestResponse::class.java)
        }

    }

    @DisplayName("퀘스트 수정 시")
    @Nested
    inner class QuestUpdateTest {

        @DisplayName("퀘스트 ID에 해당하는 퀘스트가 없으면 EntityNotFound 예외를 던진다")
        @Test
        fun `퀘스트 조회 결과가 없다면 예외가 발생한다`() {
            //given
            val mockDto = mock<QuestRequest>()
            val questId = 0L
            val userId = 0L
            doReturn(Optional.ofNullable(null)).`when`(questRepository).findById(eq(questId))

            //when
            val call: () -> Unit = { questService.updateQuest(mockDto, questId, userId)}

            //then
            assertThatThrownBy(call).isInstanceOf(EntityNotFoundException::class.java)
            verify(mockDto, times(0)).checkRangeOfDeadLine(any())
        }

        @DisplayName("데드라인 범위 체크 로직이 호출된다")
        @Test
        fun `데드라인 범위 체크 로직이 호출된다`() {
            //given
            val mockDto = mock<QuestRequest>()
            val mockQuest = mock<Quest>()
            val mockUser = mock<UserInfo>()
            val questId = 0L
            val userId = 1L

            doReturn(Optional.of(mockQuest)).`when`(questRepository).findById(eq(questId))
            doReturn(mockUser).`when`(mockQuest).user

            //when
            questService.updateQuest(mockDto, questId, userId)

            //then
            verify(mockDto, times(1)).checkRangeOfDeadLine(anyOrNull())
        }


        @DisplayName("퀘스트 소유주 검증 로직이 호출된다")
        @Test
        fun `퀘스트 소유주 검증 로직이 호출된다`() {
            //given
            val mockDto = mock<QuestRequest>()
            val mockQuest = mock<Quest>()
            val mockUser = mock<UserInfo>()
            val questId = 0L
            val userId = 1L

            doReturn(Optional.of(mockQuest)).`when`(questRepository).findById(eq(questId))
            doReturn(mockUser).`when`(mockQuest).user

            //when
            questService.updateQuest(mockDto, questId, userId)

            //then
            verify(mockQuest, times(1)).checkOwnershipOrThrow(eq(userId))
        }


        @DisplayName("퀘스트 진행 상태 검증 로직이 호출된다")
        @Test
        fun `퀘스트 진행 상태 검증 로직이 호출된다`() {
            //given
            val mockDto = mock<QuestRequest>()
            val mockQuest = mock<Quest>()
            val mockUser = mock<UserInfo>()
            val questId = 0L
            val userId = 1L

            doReturn(Optional.of(mockQuest)).`when`(questRepository).findById(eq(questId))
            doReturn(mockUser).`when`(mockQuest).user

            //when
            questService.updateQuest(mockDto, questId, userId)

            //then
            verify(mockQuest, times(1)).checkStateIsProceedOrThrow()
        }

        @DisplayName("기존 퀘스트가 메인 퀘스트라면 타입 변경 로직이 호출된다")
        @Test
        fun `메인 퀘스트라면 dto의 타입을 변경한다`() {
            //given
            val mockDto = mock<QuestRequest>()
            val mockQuest = mock<Quest>()
            val mockUser = mock<UserInfo>()

            val questId = 0L
            val userId = 0L

            doReturn(mockUser).`when`(mockQuest).user
            doReturn(Optional.of(mockQuest)).`when`(questRepository).findById(eq(questId))

            doReturn(true).`when`(mockQuest).isMainQuest()

            //when
            questService.updateQuest(mockDto, questId, userId)

            //then
            verify(mockQuest, times(1)).isMainQuest()
            verify(mockDto, times(1)).toMainQuest()
        }

        @DisplayName("기존 퀘스트가 서브 퀘스트라면 타입 변경 로직이 호출되지 않는다")
        @Test
        fun `메인 퀘스트가 아니면 dto의 타입을 변경하지 않는다`() {
            //given
            val mockDto = mock<QuestRequest>()
            val mockQuest = mock<Quest>()
            val mockUser = mock<UserInfo>()

            val questId = 0L
            val userId = 0L

            doReturn(mockUser).`when`(mockQuest).user
            doReturn(Optional.of(mockQuest)).`when`(questRepository).findById(eq(questId))

            doReturn(false).`when`(mockQuest).isMainQuest()

            //when
            questService.updateQuest(mockDto, questId, userId)

            //then
            verify(mockQuest, times(1)).isMainQuest()
            verify(mockDto, times(0)).toMainQuest()
        }

        @DisplayName("정상 호출일 경우 퀘스트 업데이트 로직이 호출된다")
        @Test
        fun `퀘스트 업데이트 로직이 호출된다`() {
            //given
            val mockDto = mock<QuestRequest>()
            val mockQuest = mock<Quest>()
            val mockUser = mock<UserInfo>()

            val questId = 0L
            val userId = 0L

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
            val userId = 0L
            doReturn(Optional.ofNullable(null)).`when`(questRepository).findById(eq(questId))

            //when
            val call = { questService.deleteQuest(questId, userId) }

            //then
            assertThatThrownBy(call).isInstanceOf(EntityNotFoundException::class.java)
        }

        @DisplayName("퀘스트 소유주 검증 로직이 호출된다")
        @Test
        fun `퀘스트 소유주 검증 로직이 호출된다`() {
            //given
            val questId = 0L
            val userId = 1L
            val mockQuest = mock<Quest>()
            doReturn(Optional.of(mockQuest)).`when`(questRepository).findById(eq(questId))

            //when
            questService.deleteQuest(questId, userId)

            //then
            verify(mockQuest, times(1)).checkOwnershipOrThrow(eq(userId))
        }

        @DisplayName("정상 호출일 경우 퀘스트 삭제 로직이 호출된다")
        @Test
        fun `퀘스트 삭제 로직이 호출된다`() {
            //given
            val questId = 0L
            val userId = 0L
            val mockQuest = mock<Quest>()
            doReturn(Optional.of(mockQuest)).`when`(questRepository).findById(eq(questId))

            //when
            questService.deleteQuest(questId, userId)

            //then
            verify(mockQuest, times(1)).deleteQuest()
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
            val userId = 0L
            doReturn(Optional.ofNullable(null)).`when`(questRepository).findById(eq(questId))

            //when
            val call = { questService.completeQuest(questId, userId) }

            //then
            assertThatThrownBy(call).isInstanceOf(EntityNotFoundException::class.java)
        }

        @DisplayName("퀘스트 소유주 검증 로직이 호출된다")
        @Test
        fun `퀘스트 소유주 검증 로직이 호출된다`() {
            //given
            val questId = 0L
            val userId = 1L
            val mockQuest = mock<Quest>()
            doReturn(Optional.of(mockQuest)).`when`(questRepository).findById(eq(questId))

            //when
            questService.completeQuest(questId, userId)

            //then
            verify(mockQuest, times(1)).checkOwnershipOrThrow(eq(userId))
        }

        @DisplayName("정상 호출일 경우 퀘스트 완료 로직이 호출된다")
        @Test
        fun `퀘스트 완료 로직이 호출된다`() {
            //given
            val questId = 0L
            val userId = 0L
            val mockQuest = mock<Quest>()
            val mockUser = mock<UserInfo>()
            val mockType = mock<QuestType>()

            doReturn(Optional.of(mockQuest)).`when`(questRepository).findById(eq(questId))
            doReturn(mockUser).`when`(mockQuest).user
            doReturn(mockType).`when`(mockQuest).type

            //when
            questService.completeQuest(questId, userId)

            //then
            verify(mockQuest, times(1)).completeQuest()
            verify(userService, times(1)).earnExpAndGold(eq(mockType), eq(mockUser))
            verify(questLogService, times(1)).saveQuestLog(eq(mockQuest))
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
            val userId = 0L
            doReturn(Optional.ofNullable(null)).`when`(questRepository).findById(eq(questId))

            //when
            val call = { questService.discardQuest(questId, userId) }

            //then
            assertThatThrownBy(call).isInstanceOf(EntityNotFoundException::class.java)
        }

        @DisplayName("퀘스트 소유주 검증 로직이 호출된다")
        @Test
        fun `퀘스트 소유주 검증 로직이 호출된다`() {
            //given
            val questId = 0L
            val userId = 1L
            val mockQuest = mock<Quest>()
            doReturn(Optional.of(mockQuest)).`when`(questRepository).findById(eq(questId))

            //when
            questService.discardQuest(questId, userId)

            //then
            verify(mockQuest, times(1)).checkOwnershipOrThrow(eq(userId))
        }

        @DisplayName("정상 호출일 경우 퀘스트 포기 로직이 호출된다")
        @Test
        fun `퀘스트 포기 로직이 호출된다`() {
            //given
            val questId = 0L
            val userId = 0L
            val mockQuest = mock<Quest>()

            doReturn(Optional.of(mockQuest)).`when`(questRepository).findById(eq(questId))

            //when
            questService.discardQuest(questId, userId)

            //then
            verify(mockQuest, times(1)).discardQuest()
            verify(questLogService, times(1)).saveQuestLog(eq(mockQuest))
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
            val userId = 0L
            doReturn(Optional.ofNullable(null)).`when`(questRepository).findById(eq(questId))

            //when
            val call: () -> Unit = { questService.interactWithDetailQuest(userId, questId, 1L, DetailInteractRequest()) }

            //then
            assertThatThrownBy(call).isInstanceOf(EntityNotFoundException::class.java)
        }


        @DisplayName("퀘스트 소유주 검증 로직이 호출된다")
        @Test
        fun `퀘스트 소유주 검증 로직이 호출된다`() {
            //given
            val questId = 0L
            val userId = 1L
            val detailQuestId = 0L
            val mockDto = mock<DetailInteractRequest>()
            val mockQuest = mock<Quest>()
            doReturn(Optional.of(mockQuest)).`when`(questRepository).findById(eq(questId))

            //when
            questService.interactWithDetailQuest(userId, questId, detailQuestId, mockDto)

            //then
            verify(mockQuest, times(1)).checkOwnershipOrThrow(eq(userId))
        }

        @DisplayName("정상 호출일 경우 세부 퀘스트 상호 작용 로직이 호출된다")
        @Test
        fun `세부 퀘스트 상호 작용 로직이 호출된다`() {
            //given
            val questId = 0L
            val userId = 1L
            val detailQuestId = 0L
            val mockDto = mock<DetailInteractRequest>()
            val mockQuest = mock<Quest>()

            doReturn(Optional.of(mockQuest)).`when`(questRepository).findById(eq(questId))

            //when
            questService.interactWithDetailQuest(userId, questId, detailQuestId, mockDto)

            //then
            verify(mockQuest, times(1)).interactWithDetailQuest(eq(detailQuestId), eq(mockDto))
        }
    }


}