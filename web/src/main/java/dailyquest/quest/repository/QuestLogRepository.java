package dailyquest.quest.repository;

import dailyquest.quest.entity.QuestLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestLogRepository extends JpaRepository<QuestLog, Long>, QuestLogRepositoryCustom {
}
