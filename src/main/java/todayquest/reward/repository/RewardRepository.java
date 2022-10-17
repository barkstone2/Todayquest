package todayquest.reward.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import todayquest.reward.entity.Reward;

import java.util.List;

public interface RewardRepository extends JpaRepository<Reward, Long> {

    List<Reward> findByUserId(Long userId);
    Reward findByIdAndUserId(Long id, Long userId);
    void deleteByIdAndUserId(Long id, Long userId);
}
