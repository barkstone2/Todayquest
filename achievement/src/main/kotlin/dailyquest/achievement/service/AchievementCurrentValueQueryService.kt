package dailyquest.achievement.service

import dailyquest.achievement.entity.AchievementType
import dailyquest.achievement.repository.AchievementCurrentValueRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Transactional(readOnly = true)
@Service
class AchievementCurrentValueQueryService(
    private val achievementCurrentValueRepository: AchievementCurrentValueRepository
) {
    fun getCurrentValueOfUser(userId: Long, achievementType: AchievementType): Int {
        val achievementCurrentValue = achievementCurrentValueRepository.findByIdOrNull(userId)
        return achievementCurrentValue?.getValueForType(achievementType) ?: 0
    }
}