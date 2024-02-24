package dailyquest.achievement.service

import dailyquest.achievement.dto.AchievementRequest
import dailyquest.achievement.entity.Achievement
import dailyquest.achievement.repository.AchievementRepository
import org.springframework.stereotype.Service

@Service
class AchievementQueryService(
    val achievementRepository: AchievementRepository
) {
    fun getAchievableAchievements(request: AchievementRequest): List<Achievement> {
        return achievementRepository.getAchievableAchievements(request.type, request.currentValue)
    }
}