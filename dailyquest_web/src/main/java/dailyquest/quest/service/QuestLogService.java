package dailyquest.quest.service;

import dailyquest.common.DateTimeExtensionKt;
import dailyquest.quest.dto.QuestLogSearchCondition;
import dailyquest.quest.dto.QuestStatisticsResponse;
import dailyquest.quest.entity.Quest;
import dailyquest.quest.entity.QuestLog;
import dailyquest.quest.entity.QuestState;
import dailyquest.quest.repository.QuestLogRepository;
import dailyquest.status.dto.StatusResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@RequiredArgsConstructor
@Transactional
@Service
public class QuestLogService {
    private final QuestLogRepository questLogRepository;

    public void saveQuestLog(Quest quest) {
        questLogRepository.save(new QuestLog(quest));
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

    public Integer getTotalRegistrationCount(Long userId) {
        return questLogRepository.countByUserIdAndState(userId, QuestState.PROCEED);
    }

    public Integer getTotalCompletionCount(Long userId) {
        return questLogRepository.countByUserIdAndState(userId, QuestState.COMPLETE);
    }

    public Integer getRegistrationDaysSince(Long userId, int beforeDays) {
        LocalDate fromDate = calculateFromDate(beforeDays);
        return questLogRepository.getDistinctRegistrationDateCountFrom(fromDate, userId);
    }

    private LocalDate calculateFromDate(int beforeDays) {
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();
        if (now.isBefore(now.withHour(6).withMinute(0).withSecond(0).withNano(0))) {
            today = today.minusDays(1);
        }
        return today.minusDays(beforeDays);
    }

    public Integer getCompletionDaysSince(Long userId, int beforeDays) {
        LocalDate fromDate = calculateFromDate(beforeDays);
        return questLogRepository.getDistinctCompletionDateCountFrom(fromDate, userId);
    }


}
