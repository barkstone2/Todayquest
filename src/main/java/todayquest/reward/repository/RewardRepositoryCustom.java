package todayquest.reward.repository;

import todayquest.reward.entity.Reward;

import java.util.List;

public interface RewardRepositoryCustom {

    List<Reward> findAllByIdAndUserId(List<Long> ids, Long userId);
}
