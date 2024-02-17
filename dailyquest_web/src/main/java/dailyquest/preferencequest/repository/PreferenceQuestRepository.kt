package dailyquest.preferencequest.repository

import dailyquest.preferencequest.dto.PreferenceQuestResponse
import dailyquest.preferencequest.entity.PreferenceQuest
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface PreferenceQuestRepository : JpaRepository<PreferenceQuest, Long> {
    fun findByIdAndUserIdAndDeletedDateIsNull(preferenceQuestId: Long, userId: Long): PreferenceQuest?

    @Query("select new dailyquest.preferencequest.dto.PreferenceQuestResponse(pq, count(q.id)) " +
            "from PreferenceQuest pq " +
            "left join Quest q on q.preferenceQuest.id = pq.id " +
            "where pq.user.id = :userId and pq.deletedDate is null " +
            "group by pq.id")
    fun getActiveEntitiesByUserIdWithUsedCount(@Param("userId") userId: Long): List<PreferenceQuestResponse>
}