package dailyquest.achievement.service

import dailyquest.achievement.dto.AchievementResponse
import dailyquest.achievement.repository.AchievementAchieveLogRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Transactional(readOnly = true)
@Service
class AchievementAchieveLogQueryService(
    private val achievementAchieveLogRepository: AchievementAchieveLogRepository,
) {
    fun getAchievedAchievements(userId: Long, pageable: Pageable): Page<AchievementResponse> {
        return achievementAchieveLogRepository.getAchievedLogs(userId, pageable).map {
            AchievementResponse.from(it)
        }
    }
}