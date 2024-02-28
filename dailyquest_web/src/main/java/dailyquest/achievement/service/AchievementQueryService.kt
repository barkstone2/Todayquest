package dailyquest.achievement.service

import dailyquest.achievement.entity.Achievement
import dailyquest.achievement.entity.AchievementType
import dailyquest.achievement.repository.AchievementRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Transactional(readOnly = true)
@Service
class AchievementQueryService(
    val achievementRepository: AchievementRepository
) {
    fun getNotAchievedAchievement(type: AchievementType, userId: Long): Achievement? {
        return achievementRepository.findNotAchievedAchievement(type, userId)
    }
}