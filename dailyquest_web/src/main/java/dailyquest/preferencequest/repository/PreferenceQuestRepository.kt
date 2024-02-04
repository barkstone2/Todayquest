package dailyquest.preferencequest.repository

import dailyquest.preferencequest.entity.PreferenceQuest
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PreferenceQuestRepository : JpaRepository<PreferenceQuest, Long> {
    fun findAllByUserIdAndDeletedDateIsNull(userId: Long): List<PreferenceQuest>
    fun findByIdAndUserIdAndDeletedDateIsNull(preferenceQuestId: Long, userId: Long): PreferenceQuest?
}