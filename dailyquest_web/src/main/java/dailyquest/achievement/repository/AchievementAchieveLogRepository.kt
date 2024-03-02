package dailyquest.achievement.repository

import dailyquest.achievement.entity.AchievementAchieveLog
import org.springframework.data.jpa.repository.JpaRepository

interface AchievementAchieveLogRepository: JpaRepository<AchievementAchieveLog, Long> {
}