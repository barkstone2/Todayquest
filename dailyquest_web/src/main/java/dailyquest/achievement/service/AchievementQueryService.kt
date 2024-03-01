package dailyquest.achievement.service

import dailyquest.achievement.dto.AchievementResponse
import dailyquest.achievement.entity.Achievement
import dailyquest.achievement.entity.AchievementType
import dailyquest.achievement.repository.AchievementRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Transactional(readOnly = true)
@Service
class AchievementQueryService(
    private val achievementRepository: AchievementRepository
) {
    fun getNotAchievedAchievement(type: AchievementType, userId: Long): Achievement {
        return achievementRepository.findNotAchievedAchievement(type, userId) ?: Achievement(type = AchievementType.EMPTY, targetValue = 0)
    }

    fun getAchievementsWithAchieveInfo(type: AchievementType, userId: Long): List<AchievementResponse> {
        return achievementRepository.getAchievementsWithAchieveInfo(type, userId)
            .sortedWith(compareBy<AchievementResponse, LocalDateTime?>(nullsLast()) { it.achievedDate }
                .thenBy { it.targetValue })
    }
}