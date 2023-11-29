package dailyquest.quest.repository;

import dailyquest.quest.dto.QuestLogSearchCondition;
import dailyquest.quest.dto.QuestStatisticsResponse;
import dailyquest.status.dto.StatusResponse;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuestLogRepositoryCustom {

    List<QuestStatisticsResponse> getGroupedQuestLogs(Long userId, QuestLogSearchCondition condition);
    StatusResponse getTotalStatisticsOfUser(@Param("userId") Long userId);
}
