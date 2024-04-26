package dailyquest.achievement.repository

import dailyquest.achievement.entity.AchievementAchieveLog
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface AchievementAchieveLogRepository: JpaRepository<AchievementAchieveLog, Long> {
    @Query("select aal from AchievementAchieveLog aal inner join Achievement a on a.inactivated = false and a.id = aal.achievement.id where aal.userId = :userId order by aal.createdDate desc")
    fun getAchievedLogs(@Param("userId") userId: Long, pageable: Pageable): Page<AchievementAchieveLog>
}