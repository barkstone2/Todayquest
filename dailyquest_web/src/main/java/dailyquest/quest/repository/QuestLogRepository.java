package dailyquest.quest.repository;

import dailyquest.quest.entity.QuestLog;
import dailyquest.quest.entity.QuestState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface QuestLogRepository extends JpaRepository<QuestLog, Long>, QuestLogRepositoryCustom {

    Integer countByUserIdAndState(Long userId, QuestState state);

    @Query("select count(distinct ql.loggedDate) from QuestLog ql where ql.userId = :userId and ql.loggedDate >= :startDate")
    Integer getDistinctDateCount(@Param("userId") Long userId, @Param("startDate") LocalDate startDate);
}
