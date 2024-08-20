package dailyquest.quest.repository

import dailyquest.quest.entity.Quest
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime

interface QuestRepository : JpaRepository<Quest, Long> {

    @Query("select q from Quest q where q.state = 'PROCEED' and q.deadLine = null and q.createdDate <= :resetDateTime")
    fun getQuestsForResetBatch(
        @Param("resetDateTime") resetDateTime: LocalDateTime,
        pageable: Pageable
    ): Page<Quest>

    @Query("select q from Quest q where q.state = 'PROCEED' and q.deadLine <= :targetDate")
    fun getQuestForDeadLineBatch(
        @Param("targetDate") targetDate: LocalDateTime,
        pageable: Pageable
    ): Page<Quest>
}
