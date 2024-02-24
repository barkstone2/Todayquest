package dailyquest.achievement.repository

import dailyquest.achievement.entity.AchievementLog
import org.springframework.data.jpa.repository.JpaRepository

interface AchievementLogRepository: JpaRepository<AchievementLog, Long> {
}