package dailyquest.quest.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import dailyquest.quest.entity.QQuest;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import java.util.Optional;

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
        QQuest quest = QQuest.quest;

        return Optional.ofNullable(query.select(quest.seq.max())
                .from(quest)
                .where(quest.user.id.eq(userId))
                .fetchOne()).orElse(0L) + 1L;
    }
}
