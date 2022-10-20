package todayquest.reward.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import todayquest.reward.entity.Reward;

import java.util.List;

public interface RewardRepository extends JpaRepository<Reward, Long>, RewardRepositoryCustom {

    List<Reward> findAllByUserId(Long userId);
}
