package dailyquest.quest.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import dailyquest.quest.dto.QuestSearchCondition;
import dailyquest.quest.entity.Quest;
import dailyquest.quest.entity.QuestState;
import lombok.RequiredArgsConstructor;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static dailyquest.quest.entity.QQuest.quest;

@RequiredArgsConstructor
public class QuestRepositoryImpl implements QuestRepositoryCustom {

    private final EntityManager em;
    private JPAQueryFactory query;

    @PostConstruct
    private void init() {
        query = new JPAQueryFactory(em);
    }

    /**
     * 유저별 MAX(SEQ) 값을 가져와 +1 해서 돌려준다.
     * 동시성 문제로 인해 네임드 락을 사용한 환경에서만 호출하도록 해야함
     */
    @Override
    public Long getNextSeqOfUser(Long userId) {

        return Optional.ofNullable(query.select(quest.seq.max())
                .from(quest)
                .where(quest.userId.eq(userId))
                .fetchOne()).orElse(0L) + 1L;
    }

    @Override
    public Page<Quest> findQuestsByCondition(Long userId, QuestSearchCondition condition, Pageable pageable) {
        QuestState state = condition.state();

        LocalDateTime startDateTime = condition.getStartResetTime();
        LocalDateTime endDateTime = condition.getEndResetTime();

        BooleanExpression wherePredicate = quest.userId.eq(userId);
        if(state != null) wherePredicate = wherePredicate.and(quest.state.eq(state));
        if(startDateTime != null && endDateTime != null) {
            wherePredicate = wherePredicate.and(quest.createdDate.between(startDateTime, endDateTime));
        }
        if(startDateTime != null && endDateTime == null) {
            wherePredicate = wherePredicate.and(quest.createdDate.goe(startDateTime));
        }
        if(startDateTime == null && endDateTime != null) {
            wherePredicate = wherePredicate.and(quest.createdDate.loe(endDateTime));
        }

        List<Quest> fetch = query.select(quest)
                .from(quest)
                .where(wherePredicate)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(quest.id.desc())
                .fetch();

        Long total = query.select(quest.count())
                .from(quest)
                .where(wherePredicate)
                .fetchOne();

        return new PageImpl<>(fetch, pageable, total);
    }
}
