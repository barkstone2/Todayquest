package dailyquest.achievement.service

import dailyquest.achievement.dto.AchievementResponse
import dailyquest.achievement.repository.AchievementRepository
import dailyquest.properties.AchievementPageSizeProperties
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Transactional(readOnly = true)
@Service
class AchievementQueryService(
    private val achievementRepository: AchievementRepository,
    private val achievementPageSizeProperties: AchievementPageSizeProperties
) {
    fun getAchievedAchievements(userId: Long, page: Int): Page<AchievementResponse> {
        val pageRequest = PageRequest.of(page, achievementPageSizeProperties.size)
        return achievementRepository.getAchievedAchievements(userId, pageRequest)
    }

    fun getNotAchievedAchievements(userId: Long, page: Int): Page<AchievementResponse> {
        val pageRequest = PageRequest.of(page, achievementPageSizeProperties.size)
        return achievementRepository.getNotAchievedAchievements(userId, pageRequest)
    }
}