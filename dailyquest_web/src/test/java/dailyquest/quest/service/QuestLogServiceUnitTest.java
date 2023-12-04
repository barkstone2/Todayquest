package dailyquest.quest.service;

import dailyquest.quest.dto.QuestLogSearchCondition;
import dailyquest.quest.dto.QuestLogSearchType;
import dailyquest.quest.dto.QuestStatisticsResponse;
import dailyquest.quest.entity.QuestState;
import dailyquest.quest.entity.QuestType;
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

}
