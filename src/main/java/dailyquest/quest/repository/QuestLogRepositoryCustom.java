package dailyquest.quest.repository;

import dailyquest.quest.dto.QuestLogSearchCondition;

import java.time.LocalDate;
import java.util.Map;

public interface QuestLogRepositoryCustom {
    Map<LocalDate, Map<String, Long>> getQuestStatisticByState(Long userId, QuestLogSearchCondition condition);
    Map<LocalDate, Map<String, Long>> getQuestStatisticByType(Long userId, QuestLogSearchCondition condition);
}
