package dailyquest.quest.service;

import dailyquest.common.MessageUtil;
import dailyquest.quest.dto.DetailInteractRequest;
import dailyquest.quest.dto.DetailResponse;
import dailyquest.quest.dto.QuestRequest;
import dailyquest.quest.dto.QuestResponse;
import dailyquest.quest.entity.DetailQuest;
import dailyquest.quest.entity.Quest;
import dailyquest.quest.entity.QuestState;
import dailyquest.quest.entity.QuestType;
import dailyquest.quest.repository.QuestRepository;
import dailyquest.user.entity.UserInfo;
import dailyquest.user.repository.UserRepository;
import dailyquest.user.service.UserService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("퀘스트 커맨드 서비스 유닛 테스트")
@ExtendWith(MockitoExtension.class)
public class QuestCommandServiceUnitTest {

    @InjectMocks QuestCommandService questCommandService;
    @Mock QuestQueryService questQueryService;
    @Mock QuestRepository questRepository;
    @Mock UserRepository userRepository;
    @Mock UserService userService;
    @Mock QuestLogService questLogService;

    @DisplayName("퀘스트 저장 시")
    @Nested
    class QuestSaveTest {

        @Mock private UserInfo foundUser;
        @Mock private QuestRequest saveRequest;
        @Mock(answer = Answers.RETURNS_SMART_NULLS) private Quest saveEntity;

        @BeforeEach
        void init() {
            doReturn(foundUser).when(userRepository).getReferenceById(any());
            doReturn(saveEntity).when(saveRequest).mapToEntity(anyLong(), any());
        }


        @DisplayName("현재 시간이 유저의 코어타임이라면 타입 변경 로직을 호출한다")
        @Test
        void ifCoreTimeChangeToMainType() {
            //given
            QuestRequest mockDto = mock(QuestRequest.class);
            UserInfo mockUser = mock(UserInfo.class);
            Quest mockQuest = mock(Quest.class, Answers.RETURNS_SMART_NULLS);
            Long userId = 0L;
            Long nextSeq = 1L;

            doReturn(true).when(mockUser).isNowCoreTime();
            doReturn(mockUser).when(userRepository).getReferenceById(eq(userId));
            doReturn(nextSeq).when(questRepository).getNextSeqByUserId(eq(userId));
            doReturn(mockQuest).when(mockDto).mapToEntity(eq(nextSeq), eq(mockUser));

            //when
            QuestResponse saveQuest = questCommandService.saveQuest(mockDto, userId);

            //then
            verify(mockDto, times(1)).checkRangeOfDeadLine();
            verify(mockUser, times(1)).isNowCoreTime();
            verify(mockDto, times(1)).toMainQuest();
            verify(questRepository, times(1)).getNextSeqByUserId(eq(userId));
            verify(questRepository, times(1)).save(mockQuest);
            verify(questLogService, times(1)).saveQuestLog(mockQuest);
            assertThat(saveQuest).isInstanceOf(QuestResponse.class);
        }


        @DisplayName("현재 시간이 유저의 코어타임 아니라면 타입 변경 로직을 호출하지 않는다")
        @Test
        void ifNotCoreTimeDoesNotChangeType() {
            //given
            QuestRequest mockDto = mock(QuestRequest.class);
            UserInfo mockUser = mock(UserInfo.class);
            Quest mockQuest = mock(Quest.class, Answers.RETURNS_SMART_NULLS);
            Long userId = 0L;
            Long nextSeq = 1L;

            doReturn(false).when(mockUser).isNowCoreTime();
            doReturn(mockUser).when(userRepository).getReferenceById(eq(userId));
            doReturn(nextSeq).when(questRepository).getNextSeqByUserId(eq(userId));
            doReturn(mockQuest).when(mockDto).mapToEntity(eq(nextSeq), eq(mockUser));

            //when
            QuestResponse saveQuest = questCommandService.saveQuest(mockDto, userId);

            //then
            verify(mockDto, times(1)).checkRangeOfDeadLine();
            verify(mockUser, times(1)).isNowCoreTime();
            verify(mockDto, times(0)).toMainQuest();
            verify(questRepository, times(1)).getNextSeqByUserId(eq(userId));
            verify(questRepository, times(1)).save(mockQuest);
            verify(questLogService, times(1)).saveQuestLog(mockQuest);
            assertThat(saveQuest).isInstanceOf(QuestResponse.class);
        }
    }

    @DisplayName("퀘스트 수정 시")
    @Nested
    class QuestUpdateTest {

        private final Quest updateTarget = mock(Quest.class, Answers.RETURNS_SMART_NULLS);

        @DisplayName("쿼리 서비스를 통해 엔티티를 조회한다")
        @Test
        void getEntityViaQueryService() {
            //given
            Long questId = 1L;
            Long userId = 1L;
            doReturn(updateTarget).when(questQueryService).getProceedEntityOfUser(any(), any());

            //when
            questCommandService.updateQuest(mock(QuestRequest.class), questId, userId);

            //then
            verify(questQueryService).getProceedEntityOfUser(eq(questId), eq(userId));
        }

        @DisplayName("대상 퀘스트가 메인 퀘스트라면 DTO 타입도 메인으로 변경한다")
        @Test
        void ifQuestTypeIsMainThanChangeTypeOfDtoToMain() {
            //given
            doReturn(updateTarget).when(questQueryService).getProceedEntityOfUser(any(), any());
            doReturn(true).when(updateTarget).isMainQuest();
            QuestRequest mockDto = mock(QuestRequest.class);

            //when
            questCommandService.updateQuest(mockDto, 1L, 1L);

            //then
            verify(mockDto, times(1)).toMainQuest();
        }

        @DisplayName("대상 퀘스트가 서브 퀘스트라면 DTO 타입 변경이 발생하지 않는다")
        @Test
        void ifQuestTypeIsSubThanDoNotChangeTypeOfDto() {
            //given
            doReturn(updateTarget).when(questQueryService).getProceedEntityOfUser(any(), any());
            doReturn(false).when(updateTarget).isMainQuest();
            QuestRequest mockDto = mock(QuestRequest.class);

            //when
            questCommandService.updateQuest(mockDto, 1L, 1L);

            //then
            verify(mockDto, never()).toMainQuest();
        }

        @DisplayName("요청 DTO 의 데드라인 범위 체크 메서드가 호출된다")
        @Test
        void invokeDeadLineCheckMethod() {
            //given
            doReturn(updateTarget).when(questQueryService).getProceedEntityOfUser(any(), any());
            doReturn(false).when(updateTarget).isMainQuest();
            QuestRequest mockDto = mock(QuestRequest.class);

            //when
            questCommandService.updateQuest(mockDto, 1L, 1L);

            //then
            verify(mockDto, times(1)).checkRangeOfDeadLine();
        }

        @DisplayName("요청 DTO 정보로 updateQuestEntity 메서드가 호출된다")
        @Test
        void invokeQuestUpdateMethod() {
            //given
            doReturn(updateTarget).when(questQueryService).getProceedEntityOfUser(any(), any());
            doReturn(false).when(updateTarget).isMainQuest();
            QuestRequest requestDto = new QuestRequest("title", "desc", List.of(), null, null);

            //when
            questCommandService.updateQuest(requestDto, 1L, 1L);

            //then
            verify(updateTarget, times(1))
                    .updateQuestEntity(eq(requestDto.getTitle()), eq(requestDto.getDescription()), eq(requestDto.getDeadLine()), any());
        }
    }

    @DisplayName("퀘스트 삭제 시")
    @Nested
    class QuestDeleteTest {
        @DisplayName("쿼리 서비스를 통해 엔티티를 조회한다")
        @Test
        void getEntityViaQueryService() {
            //given
            Long questId = 1L;
            Long userId = 1L;
            Quest deleteTarget = mock(Quest.class, Answers.RETURNS_SMART_NULLS);
            doReturn(deleteTarget).when(questQueryService).getEntityOfUser(any(), any());

            //when
            questCommandService.deleteQuest(questId, userId);

            //then
            verify(questQueryService).getEntityOfUser(eq(questId), eq(userId));
        }

        @DisplayName("퀘스트 삭제 요청이 호출된다")
        @Test
        void ifQuestOfUserThanCallMethod() {
            //given
            Quest deleteTarget = mock(Quest.class, Answers.RETURNS_SMART_NULLS);
            doReturn(deleteTarget).when(questQueryService).getEntityOfUser(any(), any());

            //when
            questCommandService.deleteQuest(1L, 1L);

            //then
            verify(deleteTarget, times(1)).deleteQuest();
        }
    }

    @DisplayName("퀘스트 완료 시")
    @Nested
    class QuestCompleteTest {
        private MockedStatic<MessageUtil> messageUtil;
        private final String deleteMessage = "delete";
        private final String proceedMessage = "proceed";
        private final String notProceedMessage = "not-proceed";

        @BeforeEach
        void beforeEach() {
            messageUtil = mockStatic(MessageUtil.class);
            when(MessageUtil.getMessage(eq("quest.error.deleted"))).thenReturn(deleteMessage);
            when(MessageUtil.getMessage(eq("quest.error.complete.detail"))).thenReturn(proceedMessage);
            when(MessageUtil.getMessage(eq("quest.error.not-proceed"))).thenReturn(notProceedMessage);
        }

        @AfterEach
        void afterEach() {
            messageUtil.close();
        }

        @DisplayName("쿼리 서비스를 통해 엔티티를 조회한다")
        @Test
        void getEntityViaQueryService() {
            //given
            Long questId = 1L;
            Long userId = 1L;
            Quest completeTarget = mock(Quest.class, Answers.RETURNS_SMART_NULLS);
            doReturn(completeTarget).when(questQueryService).getEntityOfUser(any(), any());
            doReturn(QuestState.COMPLETE).when(completeTarget).completeQuest();

            //when
            questCommandService.completeQuest(questId, userId);

            //then
            verify(questQueryService).getEntityOfUser(eq(questId), eq(userId));
        }

        @DisplayName("결과 상태가 DELETE 면 IllegalStateException 예외를 던진다")
        @Test
        public void ifResultStateIsDeleteThanThrowException() throws Exception {
            //given
            Quest completeTarget = mock(Quest.class);
            doReturn(completeTarget).when(questQueryService).getEntityOfUser(any(), any());
            doReturn(QuestState.DELETE).when(completeTarget).completeQuest();

            //when
            Executable testMethod = () -> questCommandService.completeQuest(1L, 1L);

            //then
            assertThrows(IllegalStateException.class, testMethod, deleteMessage);
        }

        @DisplayName("결과 상태가 PROCEED 면 IllegalStateException 예외를 던진다")
        @Test
        public void ifResultStateIsProceedThanThrowException() throws Exception {
            //given
            Quest completeTarget = mock(Quest.class);
            doReturn(completeTarget).when(questQueryService).getEntityOfUser(any(), any());
            doReturn(QuestState.PROCEED).when(completeTarget).completeQuest();

            //when
            Executable testMethod = () -> questCommandService.completeQuest(1L, 1L);

            //then
            assertThrows(IllegalStateException.class, testMethod, proceedMessage);
        }

        @DisplayName("결과 상태가 COMPLETE 가 아니면 IllegalStateException 예외를 던진다")
        @Test
        public void ifResultStateIsNotCompleteThanThrowException() throws Exception {
            //given
            Quest completeTarget = mock(Quest.class);
            doReturn(completeTarget).when(questQueryService).getEntityOfUser(any(), any());
            doReturn(QuestState.FAIL).when(completeTarget).completeQuest();

            //when
            Executable testMethod = () -> questCommandService.completeQuest(1L, 1L);

            //then
            assertThrows(IllegalStateException.class, testMethod, notProceedMessage);
        }

        @DisplayName("결과 상태가 COMPLETE 면 추가 로직을 호출하고 변경 로그를 저장한다")
        @Test
        public void ifResultStateIsCompleteThanCallLogicAndSaveLog() throws Exception {
            //given
            Quest completeTarget = mock(Quest.class, Answers.RETURNS_SMART_NULLS);
            doReturn(completeTarget).when(questQueryService).getEntityOfUser(any(), any());
            doReturn(QuestState.COMPLETE).when(completeTarget).completeQuest();

            QuestType targetType = QuestType.MAIN;
            doReturn(targetType).when(completeTarget).getType();

            UserInfo targetOwner = mock(UserInfo.class);
            doReturn(targetOwner).when(completeTarget).getUser();

            //when
            questCommandService.completeQuest(1L, 1L);

            //then
            verify(userService, times(1)).giveExpAndGoldToUser(eq(targetType), eq(targetOwner));
            verify(questLogService, times(1)).saveQuestLog(eq(completeTarget));
        }
    }

    @DisplayName("퀘스트 포기 시")
    @Nested
    class QuestDiscardTest {
        private MockedStatic<MessageUtil> messageUtil;
        private final String deletedMessage = "deleted";
        private final String notProceedMessage = "not-proceed";

        @BeforeEach
        void beforeEach() {
            messageUtil = mockStatic(MessageUtil.class);
            when(MessageUtil.getMessage(eq("quest.error.deleted"))).thenReturn(deletedMessage);
            when(MessageUtil.getMessage(eq("quest.error.not-proceed"))).thenReturn(notProceedMessage);
        }

        @AfterEach
        void afterEach() {
            messageUtil.close();
        }

        @DisplayName("쿼리 서비스를 통해 엔티티를 조회한다")
        @Test
        void getEntityViaQueryService() {
            //given
            Long questId = 1L;
            Long userId = 1L;
            Quest discardTarget = mock(Quest.class, Answers.RETURNS_SMART_NULLS);
            doReturn(discardTarget).when(questQueryService).getEntityOfUser(any(), any());
            doReturn(QuestState.DISCARD).when(discardTarget).discardQuest();

            //when
            questCommandService.discardQuest(questId, userId);

            //then
            verify(questQueryService).getEntityOfUser(eq(questId), eq(userId));
        }

        @DisplayName("결과 상태가 DELETE 면 IllegalStateException 예외를 던진다")
        @Test
        public void ifResultStateIsDeleteThanThrowException() throws Exception {
            //given
            Quest discardTarget = mock(Quest.class);
            doReturn(discardTarget).when(questQueryService).getEntityOfUser(any(), any());
            doReturn(QuestState.DELETE).when(discardTarget).discardQuest();

            //when
            Executable testMethod = () -> questCommandService.discardQuest(1L, 1L);

            //then
            assertThrows(IllegalStateException.class, testMethod, deletedMessage);
        }

        @DisplayName("결과 상태가 PROCEED면 IllegalStateException 예외를 던진다")
        @Test
        public void ifResultStateIsProceedThanThrowException() throws Exception {
            //given
            Quest discardTarget = mock(Quest.class);
            doReturn(discardTarget).when(questQueryService).getEntityOfUser(any(), any());
            doReturn(QuestState.PROCEED).when(discardTarget).discardQuest();

            //when
            Executable testMethod = () -> questCommandService.discardQuest(1L, 1L);

            //then
            assertThrows(IllegalStateException.class, testMethod, notProceedMessage);
        }

        @DisplayName("결과 상태가 DISCARD 면 퀘스트 상태 변경 로그 저장 로직이 호출된다")
        @Test
        public void ifResultStateIsDiscardThanSaveStateChangeLog() throws Exception {
            //given
            Quest discardTarget = mock(Quest.class, Answers.RETURNS_SMART_NULLS);
            doReturn(discardTarget).when(questQueryService).getEntityOfUser(any(), any());
            doReturn(QuestState.DISCARD).when(discardTarget).discardQuest();

            //when
            questCommandService.discardQuest(1L, 1L);

            //then
            verify(questLogService, times(1)).saveQuestLog(eq(discardTarget));
        }
    }

    @DisplayName("세부 퀘스트 카운트 변경 시")
    @Nested
    class DetailQuestCountUpdateTest {
        private MockedStatic<MessageUtil> messageUtil;
        private final String badRequestMessage = "bad-request";

        @BeforeEach
        void beforeEach() {
            messageUtil = mockStatic(MessageUtil.class);
            when(MessageUtil.getMessage(eq("exception.badRequest"))).thenReturn(badRequestMessage);
        }

        @AfterEach
        void afterEach() {
            messageUtil.close();
        }

        private final Long userId = 1L;
        private final DetailInteractRequest interactRequest = new DetailInteractRequest();
        private final Quest foundEntity = mock(Quest.class);
        private final DetailQuest interactResult = mock(DetailQuest.class, Answers.RETURNS_SMART_NULLS);

        @DisplayName("쿼리 서비스를 통해 퀘스트 엔티티를 조회한다")
        @Test
        void getQuestEntityViaQueryService() {
            //given
            doReturn(foundEntity).when(questQueryService).getProceedEntityOfUser(any(), any());
            doReturn(interactResult).when(foundEntity).updateDetailQuestCount(anyLong(), any());

            //when
            questCommandService.updateDetailQuestCount(userId, interactRequest);

            //then
            verify(questQueryService).getProceedEntityOfUser(eq(interactRequest.getQuestId()), eq(userId));
        }

        @DisplayName("카운트 변경 결과가 null이면 IllegalStateException 예외를 던진다")
        @Test
        void ifResultIsNullThrowException() {
            //given
            doReturn(foundEntity).when(questQueryService).getProceedEntityOfUser(any(), any());
            doReturn(null).when(foundEntity).updateDetailQuestCount(anyLong(), any());

            //when
            Executable testMethod = () -> questCommandService.updateDetailQuestCount(userId, mock(DetailInteractRequest.class));

            //then
            assertThrows(IllegalStateException.class, testMethod, badRequestMessage);
        }

        @DisplayName("카운트 변경 결과가 null이 아니면, 응답 DTO가 반환된다")
        @Test
        void ifResultIsNotNullThenReturnResponse() {
            //given
            doReturn(foundEntity).when(questQueryService).getProceedEntityOfUser(any(), any());
            doReturn(interactResult).when(foundEntity).updateDetailQuestCount(anyLong(), any());

            //when
            DetailResponse result = questCommandService.updateDetailQuestCount(userId, mock(DetailInteractRequest.class));

            //then
            assertThat(result).isNotNull();
        }
    }
}
