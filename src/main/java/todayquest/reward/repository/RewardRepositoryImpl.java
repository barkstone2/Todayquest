package todayquest.reward.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import todayquest.reward.entity.QReward;
import todayquest.reward.entity.Reward;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import java.util.List;

@RequiredArgsConstructor
public class RewardRepositoryImpl implements RewardRepositoryCustom {

    private final EntityManager em;
    private JPAQueryFactory query;

    @PostConstruct
    private void init() {
        query = new JPAQueryFactory(em);
    }

    @Override
    public List<Reward> findAllByIdAndUserId(List<Long> ids, Long userId) {
        QReward reward = QReward.reward;

        return query.selectFrom(reward)
                .where(
                        reward.id.in(ids),
                        reward.user.id.eq(userId)
                )
                .fetch();
    }
}
