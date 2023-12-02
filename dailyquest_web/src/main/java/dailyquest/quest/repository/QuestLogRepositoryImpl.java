package dailyquest.quest.repository;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.NullExpression;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import dailyquest.quest.dto.QuestLogSearchCondition;
import dailyquest.quest.dto.QuestStatisticsResponse;
import dailyquest.quest.entity.QuestState;
import dailyquest.status.dto.StatusResponse;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static dailyquest.quest.entity.QQuestLog.questLog;

@RequiredArgsConstructor
public class QuestLogRepositoryImpl implements QuestLogRepositoryCustom {

    private final EntityManager em;
    private JPAQueryFactory query;

    @PostConstruct
    private void init() {
        query = new JPAQueryFactory(em);
    }

    public List<QuestStatisticsResponse> getGroupedQuestLogs(Long userId, QuestLogSearchCondition condition) {

        OrderSpecifier nullOrder = new OrderSpecifier<>(Order.ASC, (Expression) NullExpression.DEFAULT, OrderSpecifier.NullHandling.Default);

        List<QuestStatisticsResponse> groupedQuestLogs = new ArrayList<>();

        List<Tuple> logsByState = query
                .select(questLog.state, questLog.loggedDate, questLog.state.count())
                .from(questLog)
                .where(questLog.userId.eq(userId),
                        questLog.state.notIn(QuestState.PROCEED, QuestState.DELETE),
                        questLog.loggedDate.between(condition.getStartDateOfSearchRange(), condition.getEndDateOfSearchRange()))
                .groupBy(questLog.state, questLog.loggedDate)
                .orderBy(nullOrder)
                .fetch();

        List<Tuple> logsByType = query
                .select(questLog.type, questLog.loggedDate, questLog.type.count())
                .from(questLog)
                .where(questLog.userId.eq(userId),
                        questLog.state.notIn(QuestState.PROCEED, QuestState.DELETE),
                        questLog.loggedDate.between(condition.getStartDateOfSearchRange(), condition.getEndDateOfSearchRange()))
                .groupBy(questLog.type, questLog.loggedDate)
                .orderBy(nullOrder)
                .fetch();

        logsByState.forEach(tuple -> {
            LocalDate loggedDate = tuple.get(questLog.loggedDate);
            QuestStatisticsResponse log = new QuestStatisticsResponse(loggedDate);
            log.addStateCount(
                    tuple.get(questLog.state).name(),
                    tuple.get(questLog.state.count())
            );
            groupedQuestLogs.add(log);
        });

        logsByType.forEach(tuple -> {
            LocalDate loggedDate = tuple.get(questLog.loggedDate);
            QuestStatisticsResponse log = new QuestStatisticsResponse(loggedDate);
            log.addTypeCount(
                    tuple.get(questLog.type).name(),
                    tuple.get(questLog.type.count())
            );
            groupedQuestLogs.add(log);
        });

        return groupedQuestLogs;
    }

    @Override
    public StatusResponse getTotalStatisticsOfUser(Long userId) {

        List<Tuple> fetch = query
                .select(questLog.state, questLog.count())
                .from(questLog)
                .where(questLog.userId.eq(userId))
                .groupBy(questLog.state)
                .fetch();

        Long registeredCount = 0L;
        Long completedCount = 0L;
        Long discardedCount = 0L;
        Long failedCount = 0L;

        for (Tuple tuple : fetch) {
            QuestState questState = tuple.get(questLog.state);
            Long count = tuple.get(questLog.count());
            switch (Objects.requireNonNull(questState)) {
                case COMPLETE -> completedCount = count;
                case DISCARD -> discardedCount = count;
                case FAIL -> failedCount = count;
                case PROCEED -> registeredCount = count;
            }
        }

        return new StatusResponse(registeredCount, completedCount, discardedCount, failedCount);
    }

}
