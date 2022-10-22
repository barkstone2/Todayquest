package todayquest.quest.repository;

import org.springframework.data.repository.query.Param;
import todayquest.quest.entity.QuestState;

import java.util.Map;

public interface QuestLogRepositoryCustom {
    Map<String, Long> getQuestAnalytics(Long userId);
}
