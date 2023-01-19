package todayquest.quest.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import todayquest.quest.entity.DetailQuest;

public interface DetailQuestRepository extends JpaRepository<DetailQuest, Long> {
}
