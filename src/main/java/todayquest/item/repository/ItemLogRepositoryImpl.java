package todayquest.item.repository;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.NullExpression;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import todayquest.item.entity.QItemLog;
import todayquest.quest.entity.QQuestLog;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class ItemLogRepositoryImpl implements ItemLogRepositoryCustom {

    private final EntityManager em;
    private JPAQueryFactory query;

    @PostConstruct
    private void init() {
        query = new JPAQueryFactory(em);
    }

    @Override
    public Map<String, Long> getItemAnalytics(Long userId) {
        QItemLog itemLog = QItemLog.itemLog;

        return query.select(itemLog.type, itemLog.type.count())
                .from(itemLog)
                .where(itemLog.userId.eq(userId))
                .groupBy(itemLog.type)
                .orderBy(new OrderSpecifier<>(Order.ASC, (Expression) NullExpression.DEFAULT, OrderSpecifier.NullHandling.Default))
                .fetch()
                .stream()
                .collect(Collectors.toMap(tuple -> tuple.get(itemLog.type).name(), tuple -> tuple.get(itemLog.type.count())));
    }
}
