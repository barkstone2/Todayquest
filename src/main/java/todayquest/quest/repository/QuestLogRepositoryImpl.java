package todayquest.quest.repository;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.NullExpression;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import todayquest.quest.entity.QQuestLog;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class QuestLogRepositoryImpl implements QuestLogRepositoryCustom {

    private final EntityManager em;
    private JPAQueryFactory query;

    @PostConstruct
    private void init() {
        query = new JPAQueryFactory(em);
    }

    @Override
    public Map<String, Long> getQuestAnalytics(Long userId) {
        QQuestLog questLog = QQuestLog.questLog;

        // MySql의 group by 시 자동 file sort 로 인해 null 정렬 추가
        return query.select(questLog.state, questLog.state.count())
                .from(questLog)
                .where(questLog.userId.eq(userId))
                .groupBy(questLog.state)
                .orderBy(new OrderSpecifier<>(Order.ASC, (Expression) NullExpression.DEFAULT, OrderSpecifier.NullHandling.Default))
                .fetch()
                .stream()
                .collect(Collectors.toMap(tuple -> tuple.get(questLog.state).name(), tuple -> tuple.get(questLog.state.count())));
    }
}
