package todayquest.quest.repository;

import todayquest.quest.dto.QuestLogSearchCondition;

import java.util.Map;

public interface QuestLogRepositoryCustom {
    Map<String, Long> getQuestStatisticByState(Long userId, QuestLogSearchCondition condition);
    Map<String, Long> getQuestStatisticByType(Long userId, QuestLogSearchCondition condition);
}
