package todayquest.reward.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import todayquest.reward.entity.Reward;

import java.util.List;
import java.util.Optional;

public interface RewardRepository extends JpaRepository<Reward, Long>, RewardRepositoryCustom {

    List<Reward> findAllByUserId(Long userId);
    Optional<Reward> findByIdAndUserId(Long id, Long userId);
    long deleteByIdAndUserId(Long id, Long userId);
}
