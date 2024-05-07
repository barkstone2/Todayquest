package dailyquest.quest.service;

import dailyquest.quest.dto.QuestLogSearchCondition;
import dailyquest.quest.dto.QuestLogSearchType;
import dailyquest.quest.dto.QuestStatisticsResponse;
import dailyquest.quest.entity.QuestState;
import dailyquest.quest.entity.QuestType;
import dailyquest.quest.repository.QuestLogRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("퀘스트 로그 서비스 유닛 테스트")
public class QuestLogServiceUnitTest {

    @InjectMocks
    QuestLogService questLogService;

    @Mock
    QuestLogRepository questLogRepository;

    @DisplayName("일별 퀘스트 로그 조회 테스트")
    @Test
    public void 일별_퀘스트_로그_조회_테스트() throws Exception {
        //given
        int year = 2022;
        int month = 3;
        int day = 5;
        LocalDate today = LocalDate.of(year, month, 5);

        int yesterdayComplete = 3;
        int yesterdayFail = 4;
        int yesterdayDiscard = 2;

        int todayRegister = 3;
        int todayComplete = 3;
        int todayMain = 2;

        List<QuestStatisticsResponse> groupedLogs = new ArrayList<>();

        LocalDate yesterday = LocalDate.of(year, month, day - 1);
        QuestStatisticsResponse e = new QuestStatisticsResponse(yesterday);
        e.addStateCount(QuestState.COMPLETE.name(), yesterdayComplete);
        groupedLogs.add(e);

        e = new QuestStatisticsResponse(yesterday);
        e.addStateCount(QuestState.FAIL.name(), yesterdayFail);
        groupedLogs.add(e);

        e = new QuestStatisticsResponse(yesterday);
        e.addStateCount(QuestState.DISCARD.name(), yesterdayDiscard);
        groupedLogs.add(e);

        e = new QuestStatisticsResponse(today);
        e.addStateCount(QuestState.COMPLETE.name(), todayComplete);
        groupedLogs.add(e);

        e = new QuestStatisticsResponse(today);
        e.addStateCount(QuestState.PROCEED.name(), todayRegister);
        groupedLogs.add(e);

        e = new QuestStatisticsResponse(today);
        e.addTypeCount(QuestType.MAIN.name(), todayMain);
        groupedLogs.add(e);

        QuestLogSearchCondition condition = new QuestLogSearchCondition(QuestLogSearchType.DAILY, today);
        doReturn(groupedLogs).when(questLogRepository).getGroupedQuestLogs(any(), eq(condition));

        //when
        Map<LocalDate, QuestStatisticsResponse> questStatistic = questLogService.getQuestStatistic(1L, condition);

        //then
        verify(questLogRepository, times(1)).getGroupedQuestLogs(eq(1L), eq(condition));

        assertThat(questStatistic.keySet().size()).isEqualTo(today.lengthOfMonth());

        assertThat(questStatistic.get(today).getCompleteCount()).isEqualTo(todayComplete);

        assertThat(questStatistic.get(yesterday).getFailCount()).isEqualTo(yesterdayFail);
        assertThat(questStatistic.get(yesterday).getDiscardCount()).isEqualTo(yesterdayDiscard);
        assertThat(questStatistic.get(yesterday).getCompleteCount()).isEqualTo(yesterdayComplete);

        assertThat(questStatistic.get(today).getMainCount()).isEqualTo(todayMain);

        assertThat(questStatistic.get(today).getSubCount()).isEqualTo(0);
        assertThat(questStatistic.get(today).getFailCount()).isEqualTo(0);
        assertThat(questStatistic.get(today).getDiscardCount()).isEqualTo(0);

        assertThat(questStatistic.get(today).getTypeRatio()).isEqualTo(100);
        assertThat(questStatistic.get(today).getStateRatio()).isEqualTo(100);
    }

    @DisplayName("getRegistrationDaysSince 호출 시")
    @Nested
    class TestGetRegistrationDaysSince {
        private MockedStatic<LocalDateTime> localDateTimeMockedStatic;
        private final Long userId = 1L;
        private final int beforeDays = 1;

        @BeforeEach
        void init() {
            localDateTimeMockedStatic = mockStatic(LocalDateTime.class, Answers.CALLS_REAL_METHODS);
        }

        @AfterEach
        void close() {
            localDateTimeMockedStatic.close();
        }

        @DisplayName("현재 시간이 오늘 오전 6시 이전이라면 fromDate가 (오늘-beforeDays-1)이 된다")
        @Test
        public void ifNowIsBefore6amThenFromDateBeSubtractedOneDayMore() throws Exception {
            //given
            LocalDateTime baseTime = LocalDateTime.of(2012, 12, 12, 5, 59);
            when(LocalDateTime.now()).thenReturn(baseTime);
            LocalDate fromDate = baseTime.minusDays(beforeDays + 1).toLocalDate();

            //when
            questLogService.getRegistrationDaysSince(userId, beforeDays);

            //then
            verify(questLogRepository).getDistinctRegistrationDateCountFrom(eq(fromDate), eq(userId));
        }

        @DisplayName("현재 시간이 오늘 오전 6시라면, fromDate가 beforeDays 만큼만 뺀 날이 된다")
        @Test
        public void ifNowIs6amThenFromDateBeSubtractedOnlyBeforeDays() throws Exception {
            //given
            LocalDateTime baseTime = LocalDateTime.of(2012, 12, 12, 6, 0);
            when(LocalDateTime.now()).thenReturn(baseTime);
            LocalDate fromDate = baseTime.minusDays(beforeDays).toLocalDate();

            //when
            questLogService.getRegistrationDaysSince(userId, beforeDays);

            //then
            verify(questLogRepository).getDistinctRegistrationDateCountFrom(eq(fromDate), eq(userId));
        }

        @DisplayName("현재 시간이 오늘 오전 6시 이후라면, fromDate가 beforeDays 만큼만 뺀 날이 된다")
        @Test
        public void ifNowIsAfter6amThenFromDateBeSubtractedOnlyBeforeDays() throws Exception {
            //given
            LocalDateTime baseTime = LocalDateTime.of(2012, 12, 12, 6, 0);
            when(LocalDateTime.now()).thenReturn(baseTime);
            LocalDate fromDate = baseTime.minusDays(beforeDays).toLocalDate();

            //when
            questLogService.getRegistrationDaysSince(userId, beforeDays);

            //then
            verify(questLogRepository).getDistinctRegistrationDateCountFrom(eq(fromDate), eq(userId));
        }
    }
}
