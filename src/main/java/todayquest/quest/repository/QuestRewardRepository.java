package todayquest.quest.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import todayquest.quest.entity.QuestReward;

public interface QuestRewardRepository extends JpaRepository<QuestReward, Long> {
}
