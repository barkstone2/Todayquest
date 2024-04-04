package dailyquest.achievement.repository

import dailyquest.achievement.entity.AchievementCurrentValue
import org.springframework.data.jpa.repository.JpaRepository

interface AchievementCurrentValueRepository: JpaRepository<AchievementCurrentValue, Long> {
}