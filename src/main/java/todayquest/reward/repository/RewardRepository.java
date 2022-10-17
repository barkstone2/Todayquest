package todayquest.reward.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import todayquest.reward.entity.Reward;
import todayquest.user.entity.UserInfo;

import java.util.List;

public interface RewardRepository extends JpaRepository<Reward, Long> {

    List<Reward> findByUser(UserInfo user);
}
