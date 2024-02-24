package dailyquest.achievement.repository

import dailyquest.achievement.entity.Achievement
import dailyquest.achievement.entity.AchievementType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface AchievementRepository: JpaRepository<Achievement, Long>{
    @Query("select a from Achievement a where a.type = :type and a.targetValue <= :currentValue")
    fun getAchievableAchievements(@Param("type") type: AchievementType, @Param("currentValue") currentValue: Int): List<Achievement>
}