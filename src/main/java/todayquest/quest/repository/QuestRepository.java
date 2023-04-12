package todayquest.quest.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import todayquest.quest.entity.Quest;
import todayquest.quest.entity.QuestState;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Repository
public interface QuestRepository extends JpaRepository<Quest, Long>, QuestRepositoryCustom {
    @Query("select q from Quest q where q.user.id = :userId and q.state= :state")
    Page<Quest> getQuestsList(@Param("userId") Long userId, @Param("state") QuestState state, Pageable pageable);

    @Query("select q from Quest q where q.state = :state and q.deadLine = null and q.user.resetTime = :resetTime")
    Page<Quest> getQuestsForResetBatch(@Param("state") QuestState state, @Param("resetTime") LocalTime resetTime, Pageable pageable);

    @Query("select q from Quest q where q.state = :state and q.deadLine != null and q.deadLine <= :targetDate")
    Page<Quest> getQuestForDeadLineBatch(@Param("state") QuestState state, @Param("targetDate") LocalDateTime targetDate, Pageable pageable);

}
