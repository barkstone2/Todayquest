package dailyquest.quest.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import dailyquest.quest.entity.QuestLog;

public interface QuestLogRepository extends JpaRepository<QuestLog, Long>, QuestLogRepositoryCustom {

}
