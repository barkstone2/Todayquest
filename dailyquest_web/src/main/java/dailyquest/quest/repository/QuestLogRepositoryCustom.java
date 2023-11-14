package dailyquest.quest.repository;

import dailyquest.quest.dto.QuestLogSearchCondition;
import dailyquest.quest.dto.QuestStatisticsResponse;

import java.util.List;

public interface QuestLogRepositoryCustom {

    List<QuestStatisticsResponse> getGroupedQuestLogs(Long userId, QuestLogSearchCondition condition);
}
