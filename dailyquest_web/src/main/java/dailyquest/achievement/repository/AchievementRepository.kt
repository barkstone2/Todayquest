package dailyquest.achievement.repository

import dailyquest.achievement.entity.Achievement
import dailyquest.achievement.entity.AchievementType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface AchievementRepository: JpaRepository<Achievement, Long>{
    @Query("select a " +
            "from Achievement a " +
            "left join AchievementLog al " +
            "on al.user.id = :userId and al.achievement.id = a.id " +
            "where a.type = :type and al.achievement.id is null " +
            "order by a.targetValue")
    fun getNotAchievedAchievements(@Param("type") type: AchievementType, @Param("userId") userId: Long): List<Achievement>
}