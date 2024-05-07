package dailyquest.quest.service;

import dailyquest.quest.entity.Quest;
import dailyquest.quest.entity.QuestState;
import dailyquest.quest.repository.QuestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("퀘스트 쿼리 서비스 유닛 테스트")
@ExtendWith(MockitoExtension.class)
public class QuestQueryServiceUnitTest {

    @InjectMocks QuestQueryService questQueryService;
    @Mock QuestRepository questRepository;
    @Mock MessageSource messageSource;

    @BeforeEach
    void beforeEach() {
        lenient().doReturn("").when(messageSource).getMessage(any(), any(), any());
    }

    @DisplayName("현재 퀘스트 조회 시")
    @Nested
    class CurrentQuestTest {

        @DisplayName("현재 시간이 오늘 오전 6시보다 이전이면, 이전 리셋 타임이 어제 오전 6시로 설정된다")
        @Test
        public void ifNowIsBeforeToday6AmThanSetPrevResetTimeToYesterday6Am() {
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
        public void ifNowIsEqualToday6AmThanSetNextResetToTomorrow6Am() {
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
        public void ifNowIsAfterToday6AmThanSetNextResetToTomorrow6Am() {
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
}
