package todayquest.reward.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import todayquest.reward.entity.Reward;

import java.util.List;
import java.util.Optional;

public interface RewardRepository extends JpaRepository<Reward, Long>, RewardRepositoryCustom {

    @Query("select r from Reward r where r.user.id = :userId and r.isDeleted = false")
    List<Reward> findAllByUserId(@Param("userId") Long userId);

    @Query("select r from Reward r where r.id = :rewardId and r.isDeleted = false")
    Optional<Reward> findByIdNotDeleted(@Param("rewardId") Long rewardId);

    @Query("select count(qr.quest.id) > 0 from QuestReward qr inner join Quest q on qr.quest.id = q.id and q.state = 'PROCEED' and qr.reward.id = :rewardId")
    boolean isRewardUseInProceedQuest(@Param("rewardId") Long rewardId);
}
