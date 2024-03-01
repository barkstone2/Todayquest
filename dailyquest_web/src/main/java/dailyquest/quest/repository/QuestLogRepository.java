package dailyquest.quest.repository;

import dailyquest.quest.entity.QuestLog;
import dailyquest.quest.entity.QuestState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface QuestLogRepository extends JpaRepository<QuestLog, Long>, QuestLogRepositoryCustom {

    Integer countByUserIdAndState(Long userId, QuestState state);

    @Query("select count(distinct ql.loggedDate) from QuestLog ql where ql.userId = :userId and ql.state = 'PROCEED' and ql.loggedDate >= :fromDate")
    Integer getDistinctRegistrationDateCountFrom(@Param("fromDate") LocalDate fromDate, @Param("userId") Long userId);

    @Query("select count(distinct ql.loggedDate) from QuestLog ql where ql.userId = :userId and ql.state = 'COMPLETE' and ql.loggedDate >= :fromDate")
    Integer getDistinctCompletionDateCountFrom(@Param("fromDate") LocalDate fromDate, @Param("userId") Long userId);
}
