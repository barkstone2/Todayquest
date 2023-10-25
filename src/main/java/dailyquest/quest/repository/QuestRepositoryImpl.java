package dailyquest.quest.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
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
    public Long getNextSeqByUserId(Long userId) {

        return Optional.ofNullable(query.select(quest.seq.max())
                .from(quest)
                .where(quest.user.id.eq(userId))
                .fetchOne()).orElse(0L) + 1L;
    }

    @Override
    public Page<Quest> findQuestsByCondition(Long userId, QuestState state, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {

        BooleanExpression wherePredicate = quest.user.id.eq(userId);
        if(state != null) wherePredicate = wherePredicate.and(quest.state.eq(state));
        if(startDate != null && endDate != null) wherePredicate = wherePredicate.and(quest.createdDate.between(startDate, endDate));
        if(startDate != null && endDate == null) wherePredicate = wherePredicate.and(quest.createdDate.goe(startDate));
        if(startDate == null && endDate != null) wherePredicate = wherePredicate.and(quest.createdDate.loe(endDate));

        List<Quest> fetch = query.select(quest)
                .from(quest)
                .where(wherePredicate)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = query.select(quest.count())
                .from(quest)
                .where(wherePredicate)
                .fetchOne();

        return new PageImpl<>(fetch, pageable, total);
    }
}
