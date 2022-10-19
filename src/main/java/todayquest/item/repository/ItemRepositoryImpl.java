package todayquest.item.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import todayquest.item.entity.Item;
import todayquest.item.entity.QItem;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import java.util.List;

@RequiredArgsConstructor
public class ItemRepositoryImpl implements ItemRepositoryCustom {

    private final EntityManager em;
    private JPAQueryFactory query;

    @PostConstruct
    private void init() {
        query = new JPAQueryFactory(em);
    }

    @Override
    public List<Item> findAllByRewardIdsAndUserId(List<Long> ids, Long userId) {
        QItem item = QItem.item;

        return query.selectFrom(item)
                .where(
                        item.user.id.eq(userId),
                        item.reward.id.in(ids)
                )
                .fetch();
    }

}
