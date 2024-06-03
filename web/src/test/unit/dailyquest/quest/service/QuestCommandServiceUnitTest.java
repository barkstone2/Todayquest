package dailyquest.quest.service;

import dailyquest.quest.dto.*;
import dailyquest.quest.entity.DetailQuest;
import dailyquest.quest.entity.Quest;
import dailyquest.quest.entity.QuestState;
import dailyquest.quest.entity.QuestType;
import dailyquest.quest.repository.QuestRepository;
import dailyquest.redis.service.RedisService;
import dailyquest.user.record.service.UserRecordService;
import dailyquest.user.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("퀘스트 커맨드 서비스 유닛 테스트")
@ExtendWith(MockitoExtension.class)
public class QuestCommandServiceUnitTest {

    @InjectMocks QuestCommandService questCommandService;
    @Mock(answer = Answers.RETURNS_SMART_NULLS) QuestRepository questRepository;
    @Mock UserService userService;
    @Mock UserRecordService userRecordService;
    @Mock QuestLogService questLogService;
    @Mock MessageSource messageSource;
    @Mock RedisService redisService;
    MockedStatic<QuestLogRequest> mockedStatic;
    MockedStatic<QuestResponse> mockedQuestResponse;

    @BeforeEach
    void init() {
        mockedStatic = mockStatic(QuestLogRequest.class);
        QuestLogRequest mock = mock(QuestLogRequest.class);
        when(QuestLogRequest.from(any())).thenReturn(mock);
        mockedQuestResponse = mockStatic(QuestResponse.class);
        lenient().when(QuestResponse.createDto(any())).thenReturn(mock(QuestResponse.class));
    }

    @AfterEach
    void close() {
        mockedStatic.close();
        mockedQuestResponse.close();
    }

    @DisplayName("퀘스트 저장 시")
    @Nested
    class QuestSaveTest {

        @Mock private WebQuestRequest saveRequest;
        @Mock(answer = Answers.RETURNS_SMART_NULLS) private Quest saveEntity;

        @BeforeEach
        void init() {
            doReturn(saveEntity).when(saveRequest).mapToEntity(anyLong(), anyLong());
            doReturn(1L).when(questRepository).getNextSeqOfUser(any());
        }

        @DisplayName("유저의 퀘스트 등록 횟수 증가 로직이 호출된다")
        @Test
        public void callAddQuestRegistrationCountOfUser() {
            //given
            Long userId = 1L;

            //when
            questCommandService.saveQuest(saveRequest, userId);

            //then
            verify(userRecordService).recordQuestRegistration(eq(userId), any());
        }
    }

    @DisplayName("퀘스트 수정 시")
    @Nested
    class QuestUpdateTest {

        private final Quest updateTarget = mock(Quest.class, Answers.RETURNS_SMART_NULLS);

        @DisplayName("리포지토리 조회 결과가 null이면 EntityNotFound 예외가 발생한다")
        @Test
        void throwIfRepositoryReturnNull() {
            //given
            Long questId = 1L;
            Long userId = 1L;
            doReturn(null).when(questRepository).findByIdAndUserId(any(), any());

            //when
            Executable run = () -> questCommandService.updateQuest(mock(WebQuestRequest.class), questId, userId);

            //then
            assertThrows(EntityNotFoundException.class, run);
        }

        @DisplayName("조회된 엔티티가 proceed 상태가 아니면 IllegalState 예외가 발생한다")
        @Test
        void throwIfEntityIsNotProceed() {
            //given
            Long questId = 1L;
            Long userId = 1L;
            doReturn(updateTarget).when(questRepository).findByIdAndUserId(any(), any());
            doReturn(false).when(updateTarget).isProceed();

            //when
            Executable run = () -> questCommandService.updateQuest(mock(WebQuestRequest.class), questId, userId);

            //then
            assertThrows(IllegalStateException.class, run);
        }

        @DisplayName("요청 DTO 정보로 updateQuestEntity 메서드가 호출된다")
        @Test
        void invokeQuestUpdateMethod() {
            //given
            doReturn(updateTarget).when(questRepository).findByIdAndUserId(any(), any());
            doReturn(true).when(updateTarget).isProceed();
            WebQuestRequest requestDto = new WebQuestRequest("title", "desc", List.of(), null, null);

            //when
            questCommandService.updateQuest(requestDto, 1L, 1L);

            //then
            verify(updateTarget, times(1))
                    .updateQuestEntity(eq(requestDto));
        }
    }

    @DisplayName("퀘스트 삭제 시")
    @Nested
    class QuestDeleteTest {

        @DisplayName("리포지토리 조회 결과가 null이면 EntityNotFound 예외가 발생한다")
        @Test
        void throwIfRepositoryReturnNull() {
            //given
            Long questId = 1L;
            Long userId = 1L;
            doReturn(null).when(questRepository).findByIdAndUserId(any(), any());

            //when
            Executable run = () -> questCommandService.deleteQuest(questId, userId);

            //then
            assertThrows(EntityNotFoundException.class, run);
        }

        @DisplayName("퀘스트 삭제 요청이 호출된다")
        @Test
        void ifQuestOfUserThanCallMethod() {
            //given
            Quest deleteTarget = mock(Quest.class, Answers.RETURNS_SMART_NULLS);
            doReturn(deleteTarget).when(questRepository).findByIdAndUserId(any(), any());

            //when
            questCommandService.deleteQuest(1L, 1L);

            //then
            verify(deleteTarget, times(1)).deleteQuest();
        }
    }

    @DisplayName("퀘스트 완료 시")
    @Nested
    class QuestCompleteTest {
        private Quest completeTarget;

        @BeforeEach
        void beforeEach() {
            completeTarget = mock(Quest.class, Answers.RETURNS_SMART_NULLS);
            lenient().doReturn(completeTarget).when(questRepository).findByIdAndUserId(any(), any());
            lenient().doReturn(QuestState.COMPLETE).when(completeTarget).getState();
            lenient().doReturn(true).when(completeTarget).isMainQuest();
        }

        @DisplayName("리포지토리 조회 결과가 null이면 EntityNotFound 예외가 발생한다")
        @Test
        void throwIfRepositoryReturnNull() {
            //given
            doReturn(null).when(questRepository).findByIdAndUserId(any(), any());

            //when
            Executable run = () -> questCommandService.completeQuest(1L, 1L);

            //then
            assertThrows(EntityNotFoundException.class, run);
        }

        @DisplayName("결과 상태가 DELETE 면 IllegalStateException 예외를 던진다")
        @Test
        public void ifResultStateIsDeleteThanThrowException() {
            //given
            doReturn(QuestState.DELETE).when(completeTarget).getState();

            //when
            Executable testMethod = () -> questCommandService.completeQuest(1L, 1L);

            //then
            assertThrows(IllegalStateException.class, testMethod);
        }

        @DisplayName("결과 상태가 PROCEED 면 IllegalStateException 예외를 던진다")
        @Test
        public void ifResultStateIsProceedThanThrowException() {
            //given
            doReturn(QuestState.PROCEED).when(completeTarget).getState();

            //when
            Executable testMethod = () -> questCommandService.completeQuest(1L, 1L);

            //then
            assertThrows(IllegalStateException.class, testMethod);
        }

        @DisplayName("결과 상태가 DELETE, PROCEED, COMPLETE 가 아니면 IllegalStateException 예외를 던진다")
        @Test
        public void ifResultStateIsNotCompleteThanThrowException() {
            //given
            doReturn(QuestState.FAIL).when(completeTarget).getState();

            //when
            Executable testMethod = () -> questCommandService.completeQuest(1L, 1L);

            //then
            assertThrows(IllegalStateException.class, testMethod);
        }

        @DisplayName("결과 상태가 COMPLETE면")
        @Nested
        class TestWhenComplete {
            private final long userId = 1L;
            private final long questId = 1L;
            private final Quest saveAndFlushTarget = mock(Quest.class, Answers.RETURNS_SMART_NULLS);

            @BeforeEach
            void init() {
                lenient().doReturn(QuestState.COMPLETE).when(completeTarget).getState();
                lenient().doReturn(saveAndFlushTarget).when(questRepository).saveAndFlush(eq(completeTarget));
            }

            @DisplayName("saveAndFlush를 호출해 변경 사항을 먼저 DB에 반영한다")
            @Test
            public void callSaveAndFlush() {
                //given
                //when
                questCommandService.completeQuest(userId, questId);

                //then
                verify(questRepository).saveAndFlush(eq(completeTarget));
            }

            @DisplayName("레디스 서비스를 통해 조회한 퀘스트 클리어 경험치, 골드와 현재 퀘스트 타입으로 DTO를 생성해 유저 경험치 골드 추가 요청을 한다")
            @Test
            public void requestAddUserExpAndGoldByRedisAndCurrentType() {
                //given
                long clearExp = 1L;
                long clearGold = 1L;
                QuestType type = QuestType.SUB;
                doReturn(clearExp).when(redisService).getQuestClearExp();
                doReturn(clearGold).when(redisService).getQuestClearGold();
                doReturn(type).when(saveAndFlushTarget).getType();
                QuestCompletionUserUpdateRequest userUpdateRequest = new QuestCompletionUserUpdateRequest(clearExp, clearGold, type);

                //when
                questCommandService.completeQuest(userId, questId);

                //then
                verify(userService, times(1)).addUserExpAndGold(eq(userId), eq(userUpdateRequest));
            }

            @DisplayName("saveAndFlush를 통해 반환된 퀘스트 정보로 DTO를 생성해 퀘스트 로그 저장 요청을 한다")
            @Test
            public void requestSaveQuestLogByReturnedEntity() {
                //given
                QuestLogRequest logRequest = mock(QuestLogRequest.class);
                when(QuestLogRequest.from(eq(saveAndFlushTarget))).thenReturn(logRequest);

                //when
                questCommandService.completeQuest(userId, questId);

                //then
                verify(questLogService, times(1)).saveQuestLog(eq(logRequest));
            }

            @DisplayName("기록된 로그 날짜로 유저의 퀘스트 완료 기록을 갱신 요청한다")
            @Test
            public void requestRecordCompletionByLoggedDate() {
                //given
                QuestLogRequest questLogRequest = QuestLogRequest.from(saveAndFlushTarget);
                LocalDate loggedDate = questLogRequest.getLoggedDate();

                //when
                questCommandService.completeQuest(userId, questId);

                //then
                verify(userRecordService).recordQuestCompletion(eq(userId), eq(loggedDate));
            }
        }
    }

    @DisplayName("퀘스트 포기 시")
    @Nested
    class QuestDiscardTest {
        private final Quest discardTarget = mock(Quest.class);

        @BeforeEach
        void init() {
            lenient().doReturn(discardTarget).when(questRepository).findByIdAndUserId(any(), any());
        }

        @DisplayName("리포지토리 조회 결과가 null이면 EntityNotFound 예외가 발생한다")
        @Test
        void throwIfRepositoryReturnNull() {
            //given
            Long questId = 1L;
            Long userId = 1L;
            doReturn(null).when(questRepository).findByIdAndUserId(any(), any());

            //when
            Executable run = () -> questCommandService.discardQuest(questId, userId);

            //then
            assertThrows(EntityNotFoundException.class, run);
        }

        @DisplayName("결과 상태가 DELETE 면 IllegalStateException 예외를 던진다")
        @Test
        public void ifResultStateIsDeleteThanThrowException() {
            //given
            doReturn(QuestState.DELETE).when(discardTarget).getState();

            //when
            Executable testMethod = () -> questCommandService.discardQuest(1L, 1L);

            //then
            assertThrows(IllegalStateException.class, testMethod);
        }

        @DisplayName("결과 상태가 PROCEED면 IllegalStateException 예외를 던진다")
        @Test
        public void ifResultStateIsProceedThanThrowException() {
            //given
            doReturn(QuestState.PROCEED).when(discardTarget).getState();

            //when
            Executable testMethod = () -> questCommandService.discardQuest(1L, 1L);

            //then
            assertThrows(IllegalStateException.class, testMethod);
        }

        @DisplayName("결과 상태가 DISCARD면")
        @Nested
        class TestWhenReturnedTypeIsDiscard {
            private Quest saveAndFlushTarget = mock(Quest.class);

            @BeforeEach
            void init() {
                doReturn(QuestState.DISCARD).when(discardTarget).getState();
                doReturn(saveAndFlushTarget).when(questRepository).saveAndFlush(eq(discardTarget));
            }

            @DisplayName("saveAndFlush를 호출한다")
            @Test
            public void callSaveAndFlush() {
                //given
                //when
                questCommandService.discardQuest(1L, 1L);

                //then
                verify(questRepository).saveAndFlush(eq(discardTarget));
            }

            @DisplayName("퀘스트 상태 변경 로그 저장 로직이 호출된다")
            @Test
            public void callSaveStateChangeLog() {
                //given
                QuestLogRequest logRequest = mock(QuestLogRequest.class);
                when(QuestLogRequest.from(eq(saveAndFlushTarget))).thenReturn(logRequest);

                //when
                questCommandService.discardQuest(1L, 1L);

                //then
                verify(questLogService, times(1)).saveQuestLog(eq(logRequest));
            }
        }
    }

    @DisplayName("세부 퀘스트 카운트 변경 시")
    @Nested
    class DetailQuestCountUpdateTest {
        private final Long userId = 1L;
        private final DetailInteractRequest interactRequest = new DetailInteractRequest();
        private final Quest foundEntity = mock(Quest.class);
        private final DetailQuest interactResult = mock(DetailQuest.class, Answers.RETURNS_SMART_NULLS);

        @DisplayName("리포지토리 조회 결과가 null이면 EntityNotFound 예외가 발생한다")
        @Test
        void throwIfRepositoryReturnNull() {
            //given
            doReturn(null).when(questRepository).findByIdAndUserId(any(), any());

            //when
            Executable run = () -> questCommandService.updateDetailQuestCount(userId, interactRequest);

            //then
            assertThrows(EntityNotFoundException.class, run);
        }

        @DisplayName("조회된 엔티티가 proceed 상태가 아니면 IllegalState 예외가 발생한다")
        @Test
        void throwIfEntityIsNotProceed() {
            //given
            doReturn(foundEntity).when(questRepository).findByIdAndUserId(any(), any());
            doReturn(false).when(foundEntity).isProceed();

            //when
            Executable run = () -> questCommandService.updateDetailQuestCount(userId, interactRequest);

            //then
            assertThrows(IllegalStateException.class, run);
        }

        @DisplayName("카운트 변경 결과가 null이면 IllegalStateException 예외를 던진다")
        @Test
        void ifResultIsNullThrowException() {
            //given
            doReturn(foundEntity).when(questRepository).findByIdAndUserId(any(), any());
            doReturn(null).when(foundEntity).updateDetailQuestCount(anyLong(), any());

            //when
            Executable testMethod = () -> questCommandService.updateDetailQuestCount(userId, mock(DetailInteractRequest.class));

            //then
            assertThrows(IllegalStateException.class, testMethod);
        }

        @DisplayName("카운트 변경 결과가 null이 아니면, 응답 DTO가 반환된다")
        @Test
        void ifResultIsNotNullThenReturnResponse() {
            //given
            doReturn(foundEntity).when(questRepository).findByIdAndUserId(any(), any());
            doReturn(true).when(foundEntity).isProceed();
            doReturn(interactResult).when(foundEntity).updateDetailQuestCount(anyLong(), any());

            //when
            DetailResponse result = questCommandService.updateDetailQuestCount(userId, mock(DetailInteractRequest.class));

            //then
            assertThat(result).isNotNull();
        }
    }
}