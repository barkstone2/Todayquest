package dailyquest.quest.service;

import dailyquest.common.MessageUtil;
import dailyquest.quest.dto.*;
import dailyquest.quest.entity.DetailQuest;
import dailyquest.quest.entity.Quest;
import dailyquest.quest.entity.QuestState;
import dailyquest.quest.repository.QuestRepository;
import dailyquest.user.entity.User;
import dailyquest.user.repository.UserRepository;
import dailyquest.user.service.UserService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Answers;
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
    MockedStatic<QuestLogRequest> mockedStatic;

    @BeforeEach
    void init() {
        mockedStatic = mockStatic(QuestLogRequest.class);
        QuestLogRequest mock = mock(QuestLogRequest.class);
        when(QuestLogRequest.from(any())).thenReturn(mock);
    }

    @AfterEach
    void close() {
        mockedStatic.close();
    }

    @DisplayName("퀘스트 저장 시")
    @Nested
    class QuestSaveTest {

        @Mock private User foundUser;
        @Mock private QuestRequest saveRequest;
        @Mock(answer = Answers.RETURNS_SMART_NULLS) private Quest saveEntity;

        @BeforeEach
        void init() {
            doReturn(foundUser).when(userRepository).getReferenceById(any());
            doReturn(saveEntity).when(saveRequest).mapToEntity(anyLong(), any());
            doReturn(1L).when(questRepository).getNextSeqByUserId(any());
        }

        @DisplayName("현재 시간이 유저의 코어타임이라면 타입 변경 로직을 호출한다")
        @Test
        void ifCoreTimeChangeToMainType() {
            //given
            doReturn(true).when(foundUser).isNowCoreTime();

            //when
            questCommandService.saveQuest(saveRequest, 1L);

            //then
            verify(saveRequest, times(1)).toMainQuest();
        }

        @DisplayName("현재 시간이 유저의 코어타임 아니라면 타입 변경 로직을 호출하지 않는다")
        @Test
        void ifNotCoreTimeDoesNotChangeType() {
            //given
            doReturn(false).when(foundUser).isNowCoreTime();

            //when
            questCommandService.saveQuest(saveRequest, 1L);

            //then
            verify(saveRequest, times(0)).toMainQuest();
        }

        @DisplayName("유저의 퀘스트 등록 횟수 증가 로직이 호출된다")
        @Test
        public void callAddQuestRegistrationCountOfUser() throws Exception {
            //given
            Long userId = 1L;

            //when
            questCommandService.saveQuest(saveRequest, userId);

            //then
            verify(userService).recordQuestRegistration(eq(userId), any());
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

        private Quest completeTarget;
        private QuestCompletionRequest questCompletionRequest;

        @BeforeEach
        void beforeEach() {
            messageUtil = mockStatic(MessageUtil.class);
            when(MessageUtil.getMessage(eq("quest.error.deleted"))).thenReturn(deleteMessage);
            when(MessageUtil.getMessage(eq("quest.error.complete.detail"))).thenReturn(proceedMessage);
            when(MessageUtil.getMessage(eq("quest.error.not-proceed"))).thenReturn(notProceedMessage);
            completeTarget = mock(Quest.class, Answers.RETURNS_SMART_NULLS);
            lenient().doReturn(completeTarget).when(questQueryService).getEntityOfUser(any(), any());
            lenient().doReturn(QuestState.COMPLETE).when(completeTarget).completeQuest();
            lenient().doReturn(true).when(completeTarget).isMainQuest();
            questCompletionRequest = mock(QuestCompletionRequest.class, Answers.RETURNS_SMART_NULLS);
        }

        @AfterEach
        void afterEach() {
            messageUtil.close();
        }

        @DisplayName("쿼리 서비스를 통해 엔티티를 조회한다")
        @Test
        void getEntityViaQueryService() {
            //given
            Long userId = 1L;

            //when
            questCommandService.completeQuest(userId, questCompletionRequest);

            //then
            verify(questQueryService).getEntityOfUser(any(), eq(userId));
        }

        @DisplayName("결과 상태가 DELETE 면 IllegalStateException 예외를 던진다")
        @Test
        public void ifResultStateIsDeleteThanThrowException() throws Exception {
            //given
            doReturn(QuestState.DELETE).when(completeTarget).completeQuest();

            //when
            Executable testMethod = () -> questCommandService.completeQuest(1L, questCompletionRequest);

            //then
            assertThrows(IllegalStateException.class, testMethod, deleteMessage);
        }

        @DisplayName("결과 상태가 PROCEED 면 IllegalStateException 예외를 던진다")
        @Test
        public void ifResultStateIsProceedThanThrowException() throws Exception {
            //given
            doReturn(QuestState.PROCEED).when(completeTarget).completeQuest();

            //when
            Executable testMethod = () -> questCommandService.completeQuest(1L, questCompletionRequest);

            //then
            assertThrows(IllegalStateException.class, testMethod, proceedMessage);
        }

        @DisplayName("결과 상태가 COMPLETE 가 아니면 IllegalStateException 예외를 던진다")
        @Test
        public void ifResultStateIsNotCompleteThanThrowException() throws Exception {
            //given
            doReturn(QuestState.FAIL).when(completeTarget).completeQuest();

            //when
            Executable testMethod = () -> questCommandService.completeQuest(1L, questCompletionRequest);

            //then
            assertThrows(IllegalStateException.class, testMethod, notProceedMessage);
        }

        @DisplayName("결과 상태가 COMPLETE면 유저 경험치, 골드 증가 로직을 호출한다")
        @Test
        public void ifResultStateIsCompleteThanCallUserExpAndGoldEarn() throws Exception {
            //given
            long userId = 1L;
            doReturn(QuestState.COMPLETE).when(completeTarget).completeQuest();

            //when
            questCommandService.completeQuest(userId, questCompletionRequest);

            //then
            verify(userService, times(1)).addUserExpAndGold(eq(userId), eq(questCompletionRequest));
        }

        @DisplayName("결과 상태가 COMPLETE면 변경 로그를 저장한다")
        @Test
        public void ifResultStateIsCompleteThanSaveLog() throws Exception {
            //given
            long userId = 1L;
            doReturn(QuestState.COMPLETE).when(completeTarget).completeQuest();

            //when
            questCommandService.completeQuest(userId, questCompletionRequest);

            //then
            verify(questLogService, times(1)).saveQuestLog(any());
        }

        @DisplayName("결과 상태가 COMPLETE고 완료한 퀘스트가 MAIN 퀘스트면 완료 요청 DTO 타입을 메인으로 변경한다")
        @Test
        public void ifResultIsCompletedAndIsMainQuestThenChangeToMain() throws Exception {
            //given
            doReturn(QuestState.COMPLETE).when(completeTarget).completeQuest();
            doReturn(true).when(completeTarget).isMainQuest();

            //when
            questCommandService.completeQuest(1L, questCompletionRequest);

            //then
            verify(questCompletionRequest, times(1)).toMainQuest();
        }

        @DisplayName("결과 상태가 COMPLETE면 유저의 퀘스트 완료 횟수 증가 로직이 호출된다")
        @Test
        public void ifResultIsCompletedThenAddQuestCompletionCountOfUser() throws Exception {
            //given
            Long userId = 1L;

            //when
            questCommandService.completeQuest(userId, questCompletionRequest);

            //then
            verify(userService).recordQuestCompletion(eq(userId), any());
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
            verify(questLogService, times(1)).saveQuestLog(any());
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
