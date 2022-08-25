package todayquest.quest.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import todayquest.quest.entity.Quest;
import todayquest.user.entity.UserInfo;

import java.util.List;

@Repository
public interface QuestRepository extends JpaRepository<Quest, Long> {
    List<Quest> getQuestsByUserOrderByDeadLineDateAscDeadLineTimeAsc(UserInfo userInfo);
}
