package todayquest.quest.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import todayquest.quest.entity.QuestLog;

public interface QuestLogRepository extends JpaRepository<QuestLog, Long>, QuestLogRepositoryCustom {

}
