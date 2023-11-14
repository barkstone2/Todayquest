package dailyquest.quest.service;

import dailyquest.quest.dto.QuestLogSearchCondition;
import dailyquest.quest.dto.QuestLogSearchType;
import dailyquest.quest.dto.QuestStatisticsResponse;
import dailyquest.quest.repository.QuestLogRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    @DisplayName("일별 퀘스트 로그 조회")
    @Test
    public void 일별_퀘스트_로그_조회() throws Exception {
        //given
        int year = 2022;
        int month = 3;
        int day = 5;
        LocalDate today = LocalDate.of(year, month, 5);

        int yesterdayComplete = 3;
        int yesterdayFail = 4;
        int yesterdayDiscard = 2;

        int todayComplete = 3;
        int todayMain = 2;

        List<QuestStatisticsResponse> groupedLogs = new ArrayList<>();

        LocalDate yesterday = LocalDate.of(year, month, day - 1);
        for (int i = 0; i < yesterdayComplete; i++) {
            QuestStatisticsResponse e = new QuestStatisticsResponse(yesterday);
            e.addStateCount("COMPLETE", 1);
            groupedLogs.add(e);
        }

        for (int i = 0; i < yesterdayFail; i++) {
            QuestStatisticsResponse e = new QuestStatisticsResponse(yesterday);
            e.addStateCount("FAIL", 1);
            groupedLogs.add(e);
        }

        for (int i = 0; i < yesterdayDiscard; i++) {
            QuestStatisticsResponse e = new QuestStatisticsResponse(yesterday);
            e.addStateCount("DISCARD", 1);
            groupedLogs.add(e);
        }

        for (int i = 0; i < todayComplete; i++) {
            QuestStatisticsResponse e = new QuestStatisticsResponse(today);
            e.addStateCount("COMPLETE", 1);
            groupedLogs.add(e);
        }

        for (int i = 0; i < todayMain; i++) {
            QuestStatisticsResponse e = new QuestStatisticsResponse(today);
            e.addTypeCount("MAIN", 1);
            groupedLogs.add(e);
        }

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


}
