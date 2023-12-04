package dailyquest.quest.entity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("퀘스트 로그 엔티티 유닛 테스트")
public class QuestLogEntityUnitTest {

    @DisplayName("생성자 호출 시 현재 시간이 오전 6시 이전이면 loggedDate가 어제로 기록된다")
    @Test
    public void ifNowTimeIsBefore6AmThanSetLoggedDateToYesterday() throws Exception {
        //given
        LocalDate nowDate = LocalDate.now();
        LocalTime mockTime = LocalTime.of(5, 59);

        try (
                MockedStatic<LocalTime> ignored = mockStatic(LocalTime.class, Answers.CALLS_REAL_METHODS);
                MockedStatic<LocalDate> ignored2 = mockStatic(LocalDate.class, Answers.CALLS_REAL_METHODS)
        ) {
            when(LocalDate.now()).thenReturn(nowDate);
            when(LocalTime.now()).thenReturn(mockTime);

            //when
            QuestLog questLog = new QuestLog(1L, 1L, QuestState.COMPLETE, QuestType.MAIN);

            //then
            assertThat(questLog.getLoggedDate()).isEqualTo(nowDate.minusDays(1));
        }
    }

    @DisplayName("생성자 호출 시 현재 시간이 오전 6시와 같으면 loggedDate가 오늘로 기록된다")
    @Test
    public void ifNowTimeIsEqual6AmThanSetLoggedDateToToday() throws Exception {
        //given
        LocalDate nowDate = LocalDate.now();
        LocalTime mockTime = LocalTime.of(6, 0);

        try (
                MockedStatic<LocalTime> ignored = mockStatic(LocalTime.class, Answers.CALLS_REAL_METHODS);
                MockedStatic<LocalDate> ignored2 = mockStatic(LocalDate.class, Answers.CALLS_REAL_METHODS)
        ) {
            when(LocalDate.now()).thenReturn(nowDate);
            when(LocalTime.now()).thenReturn(mockTime);

            //when
            QuestLog questLog = new QuestLog(1L, 1L, QuestState.COMPLETE, QuestType.MAIN);

            //then
            assertThat(questLog.getLoggedDate()).isEqualTo(nowDate);
        }
    }

    @DisplayName("생성자 호출 시 현재 시간이 오전 6시 이후면 loggedDate가 오늘로 기록된다")
    @Test
    public void ifNowTimeIsAfter6AmThanSetLoggedDateToToday() throws Exception {
        //given
        LocalDate nowDate = LocalDate.now();
        LocalTime mockTime = LocalTime.of(6, 1);

        try (
                MockedStatic<LocalTime> ignored = mockStatic(LocalTime.class, Answers.CALLS_REAL_METHODS);
                MockedStatic<LocalDate> ignored2 = mockStatic(LocalDate.class, Answers.CALLS_REAL_METHODS)
        ) {
            when(LocalDate.now()).thenReturn(nowDate);
            when(LocalTime.now()).thenReturn(mockTime);

            //when
            QuestLog questLog = new QuestLog(1L, 1L, QuestState.COMPLETE, QuestType.MAIN);

            //then
            assertThat(questLog.getLoggedDate()).isEqualTo(nowDate);
        }
    }
}
