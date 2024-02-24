package dailyquest.achievement.service

import dailyquest.achievement.entity.Achievement
import dailyquest.achievement.entity.AchievementType
import dailyquest.achievement.repository.AchievementRepository
import org.springframework.stereotype.Service

@Service
class AchievementQueryService(
    val achievementRepository: AchievementRepository
) {
    fun getNotAchievedAchievements(type: AchievementType, userId: Long): List<Achievement> {
        return achievementRepository.getNotAchievedAchievements(type, userId)
    }
}