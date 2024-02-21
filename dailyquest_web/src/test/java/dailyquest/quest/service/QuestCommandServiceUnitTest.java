package dailyquest.quest.service;

import dailyquest.common.MessageUtil;
import dailyquest.quest.dto.DetailInteractRequest;
import dailyquest.quest.dto.DetailResponse;
import dailyquest.quest.dto.QuestRequest;
import dailyquest.quest.dto.QuestResponse;
import dailyquest.quest.entity.DetailQuest;
import dailyquest.quest.entity.Quest;
import dailyquest.quest.entity.QuestState;
import dailyquest.quest.repository.QuestRepository;
import dailyquest.user.entity.UserInfo;
import dailyquest.user.repository.UserRepository;
import dailyquest.user.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
    MockedStatic<MessageUtil> messageUtil;

    @BeforeEach
    void beforeEach() {
        messageUtil = mockStatic(MessageUtil.class);
        when(MessageUtil.getMessage(any())).thenReturn("");
        when(MessageUtil.getMessage(any(), any())).thenReturn("");
    }

    @AfterEach
    void afterEach() {
        messageUtil.close();
    }


    @DisplayName("퀘스트 저장 시")
    @Nested
    class QuestSaveTest {

        @DisplayName("현재 시간이 유저의 코어타임이라면 타입 변경 로직을 호출한다")
        @Test
        void ifCoreTimeChangeToMainType() {
            //given
            QuestRequest mockDto = mock(QuestRequest.class);
            UserInfo mockUser = mock(UserInfo.class);
            Quest mockQuest = mock(Quest.class);
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
            verify(mockQuest, times(1)).updateDetailQuests(any());
            verify(questLogService, times(1)).saveQuestLog(mockQuest);
            assertThat(saveQuest).isInstanceOf(QuestResponse.class);
        }


        @DisplayName("현재 시간이 유저의 코어타임 아니라면 타입 변경 로직을 호출하지 않는다")
        @Test
        void ifNotCoreTimeDoesNotChangeType() {
            //given
            QuestRequest mockDto = mock(QuestRequest.class);
            UserInfo mockUser = mock(UserInfo.class);
            Quest mockQuest = mock(Quest.class);
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
            verify(mockQuest, times(1)).updateDetailQuests(any());
            verify(questLogService, times(1)).saveQuestLog(mockQuest);
            assertThat(saveQuest).isInstanceOf(QuestResponse.class);
        }

    }

    @DisplayName("퀘스트 수정 시")
    @Nested
    class QuestUpdateTest {

        @DisplayName("쿼리 서비스를 통해 엔티티를 조회한다")
        @Test
        void getEntityViaQueryService() {
            //given
            QuestRequest mockDto = mock(QuestRequest.class);
            Long questId = 0L;
            Long userId = 0L;
            doThrow(EntityNotFoundException.class).when(questQueryService).findByIdOrThrow(eq(questId));

            //when
            Runnable call = () -> questCommandService.updateQuest(mockDto, questId, userId);

            //then
            assertThatThrownBy(call::run).isInstanceOf(EntityNotFoundException.class);
            verify(questQueryService).findByIdOrThrow(eq(questId));
            verify(mockDto, times(0)).checkRangeOfDeadLine();
        }

        @DisplayName("요청 사용자의 퀘스트가 아닐 경우 AccessDeniedException 예외를 던진다")
        @Test
        void ifNotQuestOfUserThrowException() {
            //given
            QuestRequest mockDto = mock(QuestRequest.class);
            Quest mockQuest = mock(Quest.class);
            Long questId = 0L;
            Long userId = 1L;

            doReturn(mockQuest).when(questQueryService).findByIdOrThrow(eq(questId));
            doReturn(false).when(mockQuest).isQuestOfUser(eq(userId));

            //when
            Runnable run = () -> questCommandService.updateQuest(mockDto, questId, userId);

            //then
            assertThatThrownBy(run::run).isInstanceOf(AccessDeniedException.class);
            verify(mockQuest, times(1)).isQuestOfUser(eq(userId));
        }

        @DisplayName("퀘스트 상태가 PROCEED 가 아닐 경우 예외를 던진다")
        @Test
        void ifQuestStateIsNotProceedThanThrow() {
            //given
            QuestRequest mockDto = mock(QuestRequest.class);
            Quest mockQuest = mock(Quest.class);
            Long questId = 0L;
            Long userId = 1L;

            doReturn(mockQuest).when(questQueryService).findByIdOrThrow(eq(questId));
            doReturn(true).when(mockQuest).isQuestOfUser(eq(userId));
            doReturn(false).when(mockQuest).isProceed();

            //when
            Runnable run = () -> questCommandService.updateQuest(mockDto, questId, userId);

            //then
            assertThatThrownBy(run::run).isInstanceOf(IllegalStateException.class);
            verify(mockQuest, times(1)).isQuestOfUser(eq(userId));
        }

        @DisplayName("대상 퀘스트가 메인 퀘스트라면 DTO 타입도 메인으로 변경한다")
        @Test
        void ifQuestTypeIsMainThanChangeTypeOfDtoToMain() {
            //given
            QuestRequest mockDto = mock(QuestRequest.class);
            Quest mockQuest = mock(Quest.class);
            Long questId = 0L;
            Long userId = 1L;

            doReturn(mockQuest).when(questQueryService).findByIdOrThrow(eq(questId));
            doReturn(true).when(mockQuest).isQuestOfUser(eq(userId));
            doReturn(true).when(mockQuest).isProceed();
            doReturn(true).when(mockQuest).isMainQuest();

            //when
            questCommandService.updateQuest(mockDto, questId, userId);

            //then
            verify(mockDto, times(1)).toMainQuest();
        }

        @DisplayName("대상 퀘스트가 서브 퀘스트라면 DTO 타입 변경이 발생하지 않는다")
        @Test
        void ifQuestTypeIsSubThanDoNotChangeTypeOfDto() {
            //given
            QuestRequest mockDto = mock(QuestRequest.class);
            Quest mockQuest = mock(Quest.class);
            Long questId = 0L;
            Long userId = 1L;

            doReturn(mockQuest).when(questQueryService).findByIdOrThrow(eq(questId));
            doReturn(true).when(mockQuest).isQuestOfUser(eq(userId));
            doReturn(true).when(mockQuest).isProceed();
            doReturn(false).when(mockQuest).isMainQuest();

            //when
            questCommandService.updateQuest(mockDto, questId, userId);

            //then
            verify(mockDto, times(0)).toMainQuest();
        }

        @DisplayName("요청 DTO 의 데드라인 범위 체크 메서드가 호출된다")
        @Test
        void invokeDeadLineCheckMethod() {
            //given
            QuestRequest mockDto = mock(QuestRequest.class);
            Quest mockQuest = mock(Quest.class);
            Long questId = 0L;
            Long userId = 1L;

            doReturn(mockQuest).when(questQueryService).findByIdOrThrow(eq(questId));
            doReturn(true).when(mockQuest).isQuestOfUser(eq(userId));
            doReturn(true).when(mockQuest).isProceed();
            doReturn(false).when(mockQuest).isMainQuest();

            //when
            questCommandService.updateQuest(mockDto, questId, userId);

            //then
            verify(mockDto, times(1)).checkRangeOfDeadLine();
        }

        @DisplayName("updateQuestEntity 메서드가 호출된다")
        @Test
        void invokeQuestUpdateMethod() {
            //given
            QuestRequest mockDto = mock(QuestRequest.class);
            Quest mockQuest = mock(Quest.class);
            Long questId = 0L;
            Long userId = 0L;

            doReturn(mockQuest).when(questQueryService).findByIdOrThrow(eq(questId));
            doReturn(true).when(mockQuest).isQuestOfUser(eq(userId));
            doReturn(true).when(mockQuest).isProceed();
            doReturn(false).when(mockQuest).isMainQuest();

            //when
            questCommandService.updateQuest(mockDto, questId, userId);

            //then
            verify(mockDto, times(1)).checkRangeOfDeadLine();
            verify(mockQuest, times(1)).isQuestOfUser(eq(userId));
            verify(mockQuest, times(1)).isProceed();
            verify(mockQuest, times(1)).isMainQuest();
            verify(mockQuest, times(1)).updateQuestEntity(any(), any(), any(), any());
        }
    }

    @DisplayName("퀘스트 삭제 시")
    @Nested
    class QuestDeleteTest {
        @DisplayName("쿼리 서비스를 통해 엔티티를 조회한다")
        @Test
        void getEntityViaQueryService() {
            //given
            Long questId = 0L;
            Long userId = 0L;
            doThrow(EntityNotFoundException.class).when(questQueryService).findByIdOrThrow(eq(questId));

            //when
            Runnable call = () -> questCommandService.deleteQuest(questId, userId);

            //then
            assertThatThrownBy(call::run).isInstanceOf(EntityNotFoundException.class);
        }

        @DisplayName("요청 사용자의 퀘스트가 아닐 경우 AccessDeniedException 예외를 던진다")
        @Test
        void ifNotQuestOfUserThrowException() {
            //given
            Long questId = 0L;
            Long userId = 1L;
            Quest mockQuest = mock(Quest.class);
            doReturn(mockQuest).when(questQueryService).findByIdOrThrow(eq(questId));
            doReturn(false).when(mockQuest).isQuestOfUser(eq(userId));

            //when
            Runnable run = () -> questCommandService.deleteQuest(questId, userId);

            //then
            assertThatThrownBy(run::run).isInstanceOf(AccessDeniedException.class);
            verify(mockQuest, times(1)).isQuestOfUser(eq(userId));
            verify(mockQuest, times(0)).deleteQuest();
        }

        @DisplayName("요청 사용자의 퀘스트일 경우 퀘스트 삭제 메서드가 호출된다")
        @Test
        void ifQuestOfUserThanCallMethod() {
            //given
            Long questId = 0L;
            Long userId = 1L;
            Quest mockQuest = mock(Quest.class);

            doReturn(mockQuest).when(questQueryService).findByIdOrThrow(eq(questId));
            doReturn(true).when(mockQuest).isQuestOfUser(eq(userId));

            //when
            questCommandService.deleteQuest(questId, userId);

            //then
            verify(mockQuest, times(1)).deleteQuest();
        }
    }

    @DisplayName("퀘스트 완료 시")
    @Nested
    class QuestCompleteTest {
        @DisplayName("쿼리 서비스를 통해 엔티티를 조회한다")
        @Test
        void getEntityViaQueryService() {
            //given
            Long questId = 0L;
            Long userId = 0L;
            doThrow(EntityNotFoundException.class).when(questQueryService).findByIdOrThrow(eq(questId));

            //when
            Runnable call = () -> questCommandService.completeQuest(questId, userId);

            //then
            assertThatThrownBy(call::run).isInstanceOf(EntityNotFoundException.class);
        }

        @DisplayName("요청 사용자의 퀘스트가 아닐 경우 AccessDeniedException 예외를 던진다")
        @Test
        void ifNotQuestOfUserThrowException() {
            //given
            Long questId = 0L;
            Long userId = 1L;
            Quest mockQuest = mock(Quest.class);
            doReturn(mockQuest).when(questQueryService).findByIdOrThrow(eq(questId));
            doReturn(false).when(mockQuest).isQuestOfUser(eq(userId));

            //when
            Runnable run = () -> questCommandService.completeQuest(questId, userId);

            //then
            assertThatThrownBy(run::run).isInstanceOf(AccessDeniedException.class);
            verify(mockQuest, times(1)).isQuestOfUser(eq(userId));
        }

        @DisplayName("결과 상태가 DELETE 면 IllegalStateException 예외를 던진다")
        @Test
        public void ifResultStateIsDeleteThanThrowException() throws Exception {
            //given
            Long questId = 0L;
            Long userId = 1L;
            Quest mockQuest = mock(Quest.class);
            doReturn(mockQuest).when(questQueryService).findByIdOrThrow(eq(questId));
            doReturn(true).when(mockQuest).isQuestOfUser(eq(userId));
            doReturn(QuestState.DELETE).when(mockQuest).completeQuest();

            //when
            Runnable run = () -> questCommandService.completeQuest(questId, userId);

            //then
            assertThatThrownBy(run::run).isInstanceOf(IllegalStateException.class);
            verify(userService, times(0)).earnExpAndGold(any(), any());
            verify(questLogService, times(0)).saveQuestLog(any());
        }

        @DisplayName("결과 상태가 PROCEED 면 IllegalStateException 예외를 던진다")
        @Test
        public void ifResultStateIsProceedThanThrowException() throws Exception {
            //given
            Long questId = 0L;
            Long userId = 1L;
            Quest mockQuest = mock(Quest.class);
            doReturn(mockQuest).when(questQueryService).findByIdOrThrow(eq(questId));
            doReturn(true).when(mockQuest).isQuestOfUser(eq(userId));
            doReturn(QuestState.PROCEED).when(mockQuest).completeQuest();

            //when
            Runnable run = () -> questCommandService.completeQuest(questId, userId);

            //then
            assertThatThrownBy(run::run).isInstanceOf(IllegalStateException.class);
            verify(userService, times(0)).earnExpAndGold(any(), any());
            verify(questLogService, times(0)).saveQuestLog(any());
        }

        @DisplayName("결과 상태가 COMPLETE 가 아니면 IllegalStateException 예외를 던진다")
        @Test
        public void ifResultStateIsNotCompleteThanThrowException() throws Exception {
            //given
            Long questId = 0L;
            Long userId = 1L;
            Quest mockQuest = mock(Quest.class);
            doReturn(mockQuest).when(questQueryService).findByIdOrThrow(eq(questId));
            doReturn(true).when(mockQuest).isQuestOfUser(eq(userId));
            doReturn(QuestState.FAIL).when(mockQuest).completeQuest();

            //when
            Runnable run = () -> questCommandService.completeQuest(questId, userId);

            //then
            assertThatThrownBy(run::run).isInstanceOf(IllegalStateException.class);
            verify(userService, times(0)).earnExpAndGold(any(), any());
            verify(questLogService, times(0)).saveQuestLog(any());
        }

        @DisplayName("결과 상태가 COMPLETE 면 추가 로직을 호출하고 변경 로그를 저장한다")
        @Test
        public void ifResultStateIsCompleteThanCallLogicAndSaveLog() throws Exception {
            //given
            Long questId = 0L;
            Long userId = 1L;
            Quest mockQuest = mock(Quest.class);
            doReturn(mockQuest).when(questQueryService).findByIdOrThrow(eq(questId));
            doReturn(true).when(mockQuest).isQuestOfUser(eq(userId));
            doReturn(QuestState.COMPLETE).when(mockQuest).completeQuest();

            //when
            questCommandService.completeQuest(questId, userId);

            //then
            verify(userService, times(1)).earnExpAndGold(any(), any());
            verify(questLogService, times(1)).saveQuestLog(any());
        }
    }


    @DisplayName("퀘스트 포기 시")
    @Nested
    class QuestDiscardTest {

        @DisplayName("쿼리 서비스를 통해 엔티티를 조회한다")
        @Test
        void getEntityViaQueryService() {
            //given
            Long questId = 0L;
            Long userId = 0L;
            doThrow(EntityNotFoundException.class).when(questQueryService).findByIdOrThrow(eq(questId));

            //when
            Runnable call = () -> questCommandService.discardQuest(questId, userId);

            //then
            assertThatThrownBy(call::run).isInstanceOf(EntityNotFoundException.class);
        }

        @DisplayName("요청 사용자의 퀘스트가 아닐 경우 AccessDeniedException 예외를 던진다")
        @Test
        void ifNotQuestOfUserThrowException() {
            //given
            Long questId = 0L;
            Long userId = 1L;
            Quest mockQuest = mock(Quest.class);
            doReturn(mockQuest).when(questQueryService).findByIdOrThrow(eq(questId));
            doReturn(false).when(mockQuest).isQuestOfUser(eq(userId));

            //when
            Runnable run = () -> questCommandService.discardQuest(questId, userId);

            //then
            assertThatThrownBy(run::run).isInstanceOf(AccessDeniedException.class);
            verify(mockQuest, times(1)).isQuestOfUser(eq(userId));
        }

        @DisplayName("결과 상태가 DELETE 면 IllegalStateException 예외를 던진다")
        @Test
        public void ifResultStateIsDeleteThanThrowException() throws Exception {
            //given
            Long questId = 0L;
            Long userId = 1L;
            Quest mockQuest = mock(Quest.class);
            doReturn(mockQuest).when(questQueryService).findByIdOrThrow(eq(questId));
            doReturn(true).when(mockQuest).isQuestOfUser(eq(userId));
            doReturn(QuestState.DELETE).when(mockQuest).discardQuest();

            //when
            Runnable run = () -> questCommandService.discardQuest(questId, userId);

            //then
            assertThatThrownBy(run::run).isInstanceOf(IllegalStateException.class);
            verify(questLogService, times(0)).saveQuestLog(eq(mockQuest));
        }

        @DisplayName("결과 상태가 PROCEED면 IllegalStateException 예외를 던진다")
        @Test
        public void ifResultStateIsProceedThanThrowException() throws Exception {
            //given
            Long questId = 0L;
            Long userId = 1L;
            Quest mockQuest = mock(Quest.class);
            doReturn(mockQuest).when(questQueryService).findByIdOrThrow(eq(questId));
            doReturn(true).when(mockQuest).isQuestOfUser(eq(userId));
            doReturn(QuestState.PROCEED).when(mockQuest).discardQuest();

            //when
            Runnable run = () -> questCommandService.discardQuest(questId, userId);

            //then
            assertThatThrownBy(run::run).isInstanceOf(IllegalStateException.class);
            verify(questLogService, times(0)).saveQuestLog(eq(mockQuest));
        }

        @DisplayName("결과 상태가 DISCARD 면 퀘스트 상태 변경 로그 저장 로직이 호출된다")
        @Test
        public void ifResultStateIsDiscardThanSaveStateChangeLog() throws Exception {
            //given
            Long questId = 0L;
            Long userId = 1L;
            Quest mockQuest = mock(Quest.class);
            doReturn(mockQuest).when(questQueryService).findByIdOrThrow(eq(questId));
            doReturn(true).when(mockQuest).isQuestOfUser(eq(userId));
            doReturn(QuestState.DISCARD).when(mockQuest).discardQuest();

            //when
            questCommandService.discardQuest(questId, userId);

            //then
            verify(questLogService, times(1)).saveQuestLog(eq(mockQuest));
        }
    }

    // TODO 테스트 코드 가동성 리팩터링
    @DisplayName("세부 퀘스트 상호 작용 시")
    @Nested
    class DetailQuestInteractTest {

        @DisplayName("쿼리 서비스를 통해 퀘스트 엔티티를 조회한다")
        @Test
        void getQuestEntityViaQueryService() {
            //given
            Long questId = 0L;
            Long userId = 0L;
            doThrow(EntityNotFoundException.class).when(questQueryService).findByIdOrThrow(eq(questId));

            //when
            Runnable call = () -> questCommandService.interactWithDetailQuest(userId, new DetailInteractRequest());

            //then
            assertThatThrownBy(call::run).isInstanceOf(EntityNotFoundException.class);
            verify(questQueryService, times(1)).findByIdOrThrow(eq(questId));
        }

        @DisplayName("요청 사용자의 퀘스트가 아닐 경우 AccessDeniedException 예외를 던진다")
        @Test
        void ifNotQuestOfUserThrowException() {
            //given
            Long questId = 0L;
            Long userId = 1L;
            Long detailQuestId = 0L;
            DetailInteractRequest mockDto = mock(DetailInteractRequest.class);
            Quest mockQuest = mock(Quest.class);
            doReturn(mockQuest).when(questQueryService).findByIdOrThrow(eq(questId));
            doReturn(false).when(mockQuest).isQuestOfUser(eq(userId));

            //when
            Runnable run = () -> questCommandService.interactWithDetailQuest(userId, mockDto);

            //then
            assertThatThrownBy(run::run).isInstanceOf(AccessDeniedException.class);
            verify(mockQuest, times(1)).isQuestOfUser(eq(userId));
            verify(mockQuest, times(0)).isProceed();
        }

        @DisplayName("진행 상태의 퀘스트가 아닐 경우 IllegalStateException 예외를 던진다")
        @Test
        void ifNotProceedQuestThrowException() {
            //given
            Long questId = 0L;
            Long userId = 1L;
            Long detailQuestId = 0L;
            DetailInteractRequest mockDto = mock(DetailInteractRequest.class);
            Quest mockQuest = mock(Quest.class);
            doReturn(mockQuest).when(questQueryService).findByIdOrThrow(eq(questId));
            doReturn(true).when(mockQuest).isQuestOfUser(eq(userId));
            doReturn(false).when(mockQuest).isProceed();

            //when
            Runnable run = () -> questCommandService.interactWithDetailQuest(userId, mockDto);

            //then
            assertThatThrownBy(run::run).isInstanceOf(IllegalStateException.class);
            verify(mockQuest, times(1)).isQuestOfUser(eq(userId));
            verify(mockQuest, times(1)).isProceed();
            verify(mockQuest, times(0)).interactWithDetailQuest(eq(detailQuestId), any());
        }

        @DisplayName("세부 퀘스트 상호 작용 결과가 null이면 IllegalStateException 예외를 던진다")
        @Test
        void ifInteractResultIsNullThrowException() {
            //given
            Long questId = 0L;
            Long userId = 1L;
            Long detailQuestId = 0L;
            DetailInteractRequest mockDto = mock(DetailInteractRequest.class);
            Quest mockQuest = mock(Quest.class);
            doReturn(mockQuest).when(questQueryService).findByIdOrThrow(eq(questId));
            doReturn(true).when(mockQuest).isQuestOfUser(eq(userId));
            doReturn(true).when(mockQuest).isProceed();
            doReturn(null).when(mockQuest).interactWithDetailQuest(eq(detailQuestId), any());

            //when
            Runnable run = () -> questCommandService.interactWithDetailQuest(userId, mockDto);

            //then
            assertThatThrownBy(run::run).isInstanceOf(IllegalStateException.class);
            verify(mockQuest, times(1)).isQuestOfUser(eq(userId));
            verify(mockQuest, times(1)).isProceed();
            verify(mockQuest, times(1)).interactWithDetailQuest(eq(detailQuestId), any());
        }

        @DisplayName("세부 퀘스트 상호 작용 결과가 null이 아니면, 퀘스트 완료 가능 여부가 포함된 응답 DTO가 반환된다")
        @Test
        void ifInteractResultIsNotNullRetrunResponse() {
            //given
            Long questId = 0L;
            Long userId = 1L;
            Long detailQuestId = 0L;
            DetailInteractRequest mockDto = mock(DetailInteractRequest.class);
            Quest mockQuest = mock(Quest.class);
            DetailQuest mockDetailQuest = mock(DetailQuest.class);
            doReturn(mockQuest).when(questQueryService).findByIdOrThrow(eq(questId));
            doReturn(true).when(mockQuest).isQuestOfUser(eq(userId));
            doReturn(true).when(mockQuest).isProceed();
            doReturn(mockDetailQuest).when(mockQuest).interactWithDetailQuest(eq(detailQuestId), any());
            boolean canComplete = false;
            doReturn(canComplete).when(mockQuest).canComplete();

            //when
            DetailResponse result = questCommandService.interactWithDetailQuest(userId, mockDto);

            //then
            verify(mockQuest, times(1)).isQuestOfUser(eq(userId));
            verify(mockQuest, times(1)).isProceed();
            verify(mockQuest, times(1)).interactWithDetailQuest(eq(detailQuestId), any());
            verify(mockQuest, times(1)).canComplete();
            assertThat(result.getCanCompleteParent()).isEqualTo(canComplete);
        }
    }
}
