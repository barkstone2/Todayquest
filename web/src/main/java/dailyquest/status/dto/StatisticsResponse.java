package dailyquest.status.dto;

import dailyquest.quest.dto.QuestStatisticsResponse;

import java.time.LocalDate;
import java.util.Map;

public record StatisticsResponse(
        Map<LocalDate, QuestStatisticsResponse> questStatistics,
        LocalDate selectedDate
) {}
