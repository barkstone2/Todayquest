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
            "left join AchievementLog al " +
            "on al.user.id = :userId and al.achievement.id = a.id " +
            "where a.type = :type and al.achievement.id is null " +
            "order by a.targetValue " +
            "limit 1")
    fun findNotAchievedAchievement(@Param("type") type: AchievementType, @Param("userId") userId: Long): Achievement?


    @Query("select new dailyquest.achievement.dto.AchievementResponse(a.title, a.description, a.type, a.targetValue, (al.id is not null), al.createdDate) " +
            "from Achievement a " +
            "left join AchievementLog al " +
            "on al.user.id = :userId and al.achievement.id = a.id " +
            "where a.type = :type")
    fun getAchievementsWithAchieveInfo(@Param("type") type: AchievementType, @Param("userId") userId: Long): List<AchievementResponse>
}