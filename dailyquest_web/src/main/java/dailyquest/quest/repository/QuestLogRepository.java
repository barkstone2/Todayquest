package dailyquest.quest.repository;

import dailyquest.quest.entity.QuestLog;
import dailyquest.quest.entity.QuestState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestLogRepository extends JpaRepository<QuestLog, Long>, QuestLogRepositoryCustom {
    List<QuestLog> findAllByUserIdAndState(Long userId, QuestState state);
}
