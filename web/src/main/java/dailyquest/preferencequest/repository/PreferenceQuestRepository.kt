package dailyquest.preferencequest.repository

import dailyquest.preferencequest.entity.PreferenceQuest
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface PreferenceQuestRepository : JpaRepository<PreferenceQuest, Long> {
    fun findByIdAndUserIdAndDeletedDateIsNull(preferenceQuestId: Long, userId: Long): PreferenceQuest?

    @Query("select pq from PreferenceQuest pq where pq.user.id = :userId and pq.deletedDate is null")
    fun getActivePrefQuests(@Param("userId") userId: Long): List<PreferenceQuest>

    @Query("select count(q) from PreferenceQuest pq left join Quest q on q.userId = pq.user.id and q.preferenceQuest.id = pq.id where pq.user.id = :userId and pq.deletedDate is null group by pq.id")
    fun getUsedCountOfActivePrefQuests(@Param("userId") userId: Long): List<Long>
}