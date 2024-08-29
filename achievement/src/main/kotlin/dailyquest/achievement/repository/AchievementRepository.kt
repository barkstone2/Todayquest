package dailyquest.achievement.repository

import dailyquest.achievement.entity.Achievement
import dailyquest.achievement.entity.AchievementType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface AchievementRepository: JpaRepository<Achievement, Long>{
    @Query("select a " +
            "from Achievement a " +
            "left join AchievementAchieveLog al " +
            "on al.userId = :userId and al.achievement.id = a.id " +
            "where a.type = :type and al.achievement.id is null and a.inactivated = false " +
            "order by a.targetValue " +
            "limit 1")
    fun findNotAchievedAchievement(@Param("type") type: AchievementType, @Param("userId") userId: Long): Achievement?
    fun existsByTypeAndTargetValue(type: AchievementType, targetValue: Long): Boolean

    @Query("select a from Achievement a where a.type = :type and a.inactivated = false order by a.targetValue")
    fun getAllActivatedOfType(@Param("type") type: AchievementType): List<Achievement>

    @Query(
        "select a " +
                "from Achievement a " +
                "left join AchievementAchieveLog al " +
                "on al.userId = :userId and al.achievement.id = a.id " +
                "where a.inactivated = false and al.id is null order by a.type, a.targetValue"
    )
    fun getNotAchievedAchievements(@Param("userId") userId: Long, pageable: Pageable): Page<Achievement>
    fun getAllByOrderByTypeAscTargetValueAsc(): List<Achievement>

    @Query("select a " +
            "from Achievement a " +
            "inner join AchievementAchieveLog aal " +
            "on aal.userId = :userId and aal.achievement.id = a.id " +
            "where a.inactivated = false " +
            "order by aal.createdDate desc")
    fun getAchievedAchievements(@Param("userId") userId: Long, pageable: Pageable): Page<Achievement>
}