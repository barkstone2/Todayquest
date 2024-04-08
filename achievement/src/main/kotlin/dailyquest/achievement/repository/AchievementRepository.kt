package dailyquest.achievement.repository

import dailyquest.achievement.dto.AchievementResponse
import dailyquest.achievement.entity.Achievement
import dailyquest.achievement.entity.AchievementType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface AchievementRepository: JpaRepository<Achievement, Long>{
    @Query("select a " +
            "from Achievement a " +
            "left join AchievementAchieveLog al " +
            "on al.userId = :userId and al.achievement.id = a.id " +
            "where a.type = :type and al.achievement.id is null and a.isActive = true " +
            "order by a.targetValue " +
            "limit 1")
    fun findNotAchievedAchievement(@Param("type") type: AchievementType, @Param("userId") userId: Long): Achievement?

    @Query("select new dailyquest.achievement.dto.AchievementResponse(a.id, a.title, a.description, a.type, a.targetValue, (al.id is not null), al.createdDate) " +
            "from Achievement a " +
            "left join AchievementAchieveLog al " +
            "on al.userId = :userId and al.achievement.id = a.id " +
            "where a.type = :type and a.isActive = true"
    )
    fun getAchievementsWithAchieveInfo(@Param("type") type: AchievementType, @Param("userId") userId: Long): List<AchievementResponse>
    fun existsByTypeAndTargetValue(type: AchievementType, targetValue: Long): Boolean

    @Query("select a from Achievement a where a.type = :type and a.isActive = true order by a.targetValue")
    fun getAllActivedOfType(@Param("type") type: AchievementType): List<Achievement>
}