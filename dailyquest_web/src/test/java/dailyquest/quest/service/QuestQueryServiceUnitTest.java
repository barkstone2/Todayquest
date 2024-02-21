package dailyquest.quest.service;

import dailyquest.common.MessageUtil;
import dailyquest.quest.entity.Quest;
import dailyquest.quest.entity.QuestState;
import dailyquest.quest.repository.QuestRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("퀘스트 쿼리 서비스 유닛 테스트")
@ExtendWith(MockitoExtension.class)
public class QuestQueryServiceUnitTest {

    @InjectMocks QuestQueryService questQueryService;
    @Mock QuestRepository questRepository;
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

    @DisplayName("현재 퀘스트 조회 시")
    @Nested
    class CurrentQuestTest {

        @DisplayName("현재 시간이 오늘 오전 6시보다 이전이면, 이전 리셋 타임이 어제 오전 6시로 설정된다")
        @Test
        public void ifNowIsBeforeToday6AmThanSetPrevResetTimeToYesterday6Am() throws Exception {
            //given
            Long userId = 1L;
            QuestState state = QuestState.PROCEED;
            List<Quest> list = List.of();

            LocalDate nowDate = LocalDate.now();
            LocalDateTime resetTime = LocalDateTime.of(nowDate, LocalTime.of(6, 0));

            LocalDateTime mockNow = LocalDateTime.of(nowDate, LocalTime.of(5, 59));
            try(MockedStatic<LocalDateTime> ignored = mockStatic(LocalDateTime.class, Answers.CALLS_REAL_METHODS)) {
                when(LocalDateTime.now()).thenReturn(mockNow);

                doReturn(list).when(questRepository).getCurrentQuests(any(), any(), any(), any());

                //when
                questQueryService.getCurrentQuests(userId, state);

                //then
                verify(questRepository).getCurrentQuests(eq(userId), eq(state), eq(resetTime.minusDays(1)), eq(resetTime));
            }
        }

        @DisplayName("현재 시간이 오늘 오전 6시면, 다음 리셋 타임이 내일 오전 6시로 설정된다")
        @Test
        public void ifNowIsEqualToday6AmThanSetNextResetToTomorrow6Am() throws Exception {
            //given
            Long userId = 1L;
            QuestState state = QuestState.PROCEED;
            List<Quest> list = List.of();

            LocalDate nowDate = LocalDate.now();
            LocalDateTime resetTime = LocalDateTime.of(nowDate, LocalTime.of(6, 0));

            LocalDateTime mockNow = LocalDateTime.of(nowDate, LocalTime.of(6, 0));
            try (MockedStatic<LocalDateTime> ignored = mockStatic(LocalDateTime.class, Answers.CALLS_REAL_METHODS)) {
                when(LocalDateTime.now()).thenReturn(mockNow);

                doReturn(list).when(questRepository).getCurrentQuests(any(), any(), any(), any());

                //when
                questQueryService.getCurrentQuests(userId, state);

                //then
                verify(questRepository).getCurrentQuests(eq(userId), eq(state), eq(resetTime), eq(resetTime.plusDays(1)));
            }
        }

        @DisplayName("현재 시간이 오늘 오전 6시 이후면, 다음 리셋 타임이 내일 오전 6시로 설정된다")
        @Test
        public void ifNowIsAfterToday6AmThanSetNextResetToTomorrow6Am() throws Exception {
            //given
            Long userId = 1L;
            QuestState state = QuestState.PROCEED;
            List<Quest> list = List.of();

            LocalDate nowDate = LocalDate.now();
            LocalDateTime resetTime = LocalDateTime.of(nowDate, LocalTime.of(6, 0));

            LocalDateTime mockNow = LocalDateTime.of(nowDate, LocalTime.of(6, 1));
            try (MockedStatic<LocalDateTime> ignored = mockStatic(LocalDateTime.class, Answers.CALLS_REAL_METHODS)) {
                when(LocalDateTime.now()).thenReturn(mockNow);

                doReturn(list).when(questRepository).getCurrentQuests(any(), any(), any(), any());

                //when
                questQueryService.getCurrentQuests(userId, state);

                //then
                verify(questRepository).getCurrentQuests(eq(userId), eq(state), eq(resetTime), eq(resetTime.plusDays(1)));
            }
        }
    }

    @DisplayName("getEntityOfUser 메서드 호출 시")
    @Nested
    class TestGetEntityOfUser {
        @DisplayName("조회한 엔티티가 null이면 예외를 던진다")
        @Test
        public void ifEntityIsNullThrowException() throws Exception {
            //given
            doReturn(null).when(questRepository).findByIdAndUserId(any(), any());

            //when
            Executable call = () -> questQueryService.getEntityOfUser(1L, 1L);

            //then
            assertThrows(EntityNotFoundException.class, call);
        }

        @DisplayName("조회한 엔티티가 null이 아니면 엔티티를 반환한다")
        @Test
        public void ifSucceedThenReturn() throws Exception {
            //given
            Quest foundQuest = mock(Quest.class);
            doReturn(foundQuest).when(questRepository).findByIdAndUserId(any(), any());

            //when
            Quest returnQuest = questQueryService.getEntityOfUser(1L, 1L);

            //then
            assertThat(returnQuest).isEqualTo(foundQuest);
        }
    }

    @DisplayName("getProceedEntityOfUser 호출 시")
    @Nested
    class TestGetProceedEntityOfUser {
        @DisplayName("조회한 엔티티가 null이면 예외를 던진다")
        @Test
        public void ifEntityIsNullThrowException() throws Exception {
            //given
            doReturn(null).when(questRepository).findByIdAndUserId(any(), any());

            //when
            Executable testMethod = () -> questQueryService.getProceedEntityOfUser(1L, 1L);

            //then
            assertThrows(EntityNotFoundException.class, testMethod);
        }

        @DisplayName("조회한 엔티티가 proceed 상태가 아니면 예외를 던진다")
        @Test
        public void ifResultIsNotProceedThenThrow() throws Exception {
            //given
            Quest foundEntity = mock(Quest.class);
            doReturn(foundEntity).when(questRepository).findByIdAndUserId(any(), any());
            doReturn(false).when(foundEntity).isProceed();

            //when
            Executable testMethod = () -> questQueryService.getProceedEntityOfUser(1L, 1L);

            //then
            assertThrows(IllegalStateException.class, testMethod);
        }

        @DisplayName("조회한 엔티티가 null이 아니고 proceed 상태면 엔티티를 반환한다")
        @Test
        public void ifResultExistAndIsProceedThenReturn() throws Exception {
            //given
            Quest foundEntity = mock(Quest.class);
            doReturn(foundEntity).when(questRepository).findByIdAndUserId(any(), any());
            doReturn(true).when(foundEntity).isProceed();

            //when
            Quest returnEntity = questQueryService.getProceedEntityOfUser(1L, 1L);

            //then
            assertThat(returnEntity).isEqualTo(foundEntity);
        }
    }
}
