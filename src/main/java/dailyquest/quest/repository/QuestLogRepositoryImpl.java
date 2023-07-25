package dailyquest.quest.repository;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.NullExpression;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.EnumPath;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import dailyquest.common.TimeUtilKt;
import dailyquest.quest.dto.QuestLogSearchCondition;
import dailyquest.quest.entity.QuestState;
import dailyquest.quest.entity.QuestType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.Map;
import java.util.function.Function;

import static dailyquest.quest.entity.QQuestLog.questLog;

@RequiredArgsConstructor
public class QuestLogRepositoryImpl implements QuestLogRepositoryCustom {

    private final EntityManager em;
    private JPAQueryFactory query;

    @PostConstruct
    private void init() {
        query = new JPAQueryFactory(em);
    }

    @Override
    public Map<LocalDate, Map<String, Long>> getQuestStatisticByState(Long userId, QuestLogSearchCondition condition) {
        return getQuestStatistic(userId, condition, questLog.state);
    }

    @Override
    public Map<LocalDate, Map<String, Long>> getQuestStatisticByType(Long userId, QuestLogSearchCondition condition) {
        return getQuestStatistic(userId, condition, questLog.type);
    }

    public <T extends Enum<T>> Map<LocalDate, Map<String, Long>> getQuestStatistic(Long userId, QuestLogSearchCondition condition, EnumPath<T> enumPath) {

        Map<LocalDate, Map<String, Long>> result;
        if (enumPath.getType().equals(QuestType.class)) {
            result = condition.createResponseCollectionByType();
        } else {
            result = condition.createResponseCollectionByState();
        }

        final Function<LocalDateTime, LocalDate> dateKeyTransformFunction;

        switch (condition.getSearchType()) {
            case WEEKLY -> dateKeyTransformFunction = TimeUtilKt::firstDayOfWeek;
            case MONTHLY -> dateKeyTransformFunction = (loggedDate) -> LocalDate.from(loggedDate.with(TemporalAdjusters.firstDayOfMonth()));
            default -> dateKeyTransformFunction = LocalDate::from;
        }

        // MySql의 group by 시 자동 file sort 로 인해 null 정렬 추가
        query
                .select(enumPath, Expressions.asDate(questLog.loggedDate), enumPath.count())
                .from(questLog)
                .where(questLog.userId.eq(userId),
                        questLog.state.notIn(QuestState.PROCEED, QuestState.DELETE),
                        questLog.loggedDate.between(condition.getStartDate(), condition.getEndDate()))
                .groupBy(enumPath, Expressions.asDate(questLog.loggedDate))
                .orderBy(new OrderSpecifier<>(Order.ASC, (Expression) NullExpression.DEFAULT, OrderSpecifier.NullHandling.Default))
                .fetch()
                .forEach(tuple -> {
                    LocalDateTime loggedDate = tuple.get(questLog.loggedDate);
                    LocalDate dateKey = dateKeyTransformFunction.apply(loggedDate);

                    Map<String, Long> typeMapOfDay = result.get(dateKey);
                    typeMapOfDay.compute(tuple.get(enumPath).name(), (k, v) -> v + tuple.get(enumPath.count()));
                });

        return result;
    }

}
