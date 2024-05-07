package dailyquest.quest.repository

import dailyquest.quest.entity.QuestLog
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDate

interface QuestLogRepository : JpaRepository<QuestLog, Long> {

    @Query("select qlp.userId " +
            "from QuestLog qlp " +
            "left join QuestLog qlc " +
                "on qlc.loggedDate = :loggedDate " +
                "and qlc.state = 'COMPLETE' " +
                "and qlc.userId = qlp.userId " +
                "and qlc.questId = qlp.questId " +
            "where qlp.loggedDate = :loggedDate " +
            "and qlp.state = 'PROCEED' " +
            "group by qlp.userId " +
            "having count(qlp.questId) = count(qlc.questId)")
    fun getAllUserIdsWhoAchievedPerfectDay(@Param("loggedDate") loggedDate: LocalDate, pageable: Pageable): Page<Long>
}