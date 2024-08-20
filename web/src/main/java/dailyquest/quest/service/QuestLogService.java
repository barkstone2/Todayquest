package dailyquest.quest.service;

import dailyquest.common.DateTimeExtensionKt;
import dailyquest.quest.dto.QuestLogRequest;
import dailyquest.quest.dto.QuestLogSearchCondition;
import dailyquest.quest.dto.QuestStatisticsResponse;
import dailyquest.quest.repository.QuestLogRepository;
import dailyquest.status.dto.StatusResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@RequiredArgsConstructor
@Transactional
@Service
public class QuestLogService {
    private final QuestLogRepository questLogRepository;

    public void saveQuestLog(QuestLogRequest questLogRequest) {
        questLogRepository.save(questLogRequest.mapToEntity());
    }

    public Map<LocalDate, QuestStatisticsResponse> getQuestStatistic(Long userId, QuestLogSearchCondition condition) {

        List<QuestStatisticsResponse> groupedLogs = questLogRepository.getGroupedQuestLogs(userId, condition);

        Function<LocalDate, LocalDate> dateKeyTransformFunction =
            switch (condition.getSearchType()) {
                case WEEKLY -> DateTimeExtensionKt::firstDayOfWeek;
                case MONTHLY -> DateTimeExtensionKt::firstDayOfMonth;
                default -> LocalDate::from;
            };

        Map<LocalDate, QuestStatisticsResponse> statisticsMap = condition.createResponseMapOfPeriodUnit();

        for (QuestStatisticsResponse log : groupedLogs) {
            LocalDate loggedDate = log.getLoggedDate();
            LocalDate dateKey = dateKeyTransformFunction.apply(loggedDate);

            QuestStatisticsResponse statisticsOfDay = statisticsMap.getOrDefault(dateKey, new QuestStatisticsResponse(dateKey));

            statisticsOfDay.combineCount(log);
            statisticsMap.put(dateKey, statisticsOfDay);
        }

        statisticsMap.values().forEach(
            statistic -> {
                statistic.calcStateRatio();
                statistic.calcTypeRatio();
            }
        );
        return statisticsMap;
    }

    public StatusResponse getTotalStatistics(Long userId) {
        return questLogRepository.getTotalStatisticsOfUser(userId);
    }
}
