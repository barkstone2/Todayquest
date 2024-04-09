package dailyquest.achievement.service

import dailyquest.achievement.dto.AchievementResponse
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
    fun getAchievementsWithAchieveInfo(type: AchievementType, userId: Long): List<AchievementResponse> {
        val achievedDateNullLastAsc = compareBy<AchievementResponse, LocalDateTime?>(nullsLast()) { it.achievedDate }
        val targetValueAsc = compareBy<AchievementResponse> { it.targetValue }
        val achievedDateNullLastAscThenTargetValueAsc = achievedDateNullLastAsc.then(targetValueAsc)
        return achievementRepository.getAchievementsWithAchieveInfo(type, userId)
            .sortedWith(achievedDateNullLastAscThenTargetValueAsc)
    }

    fun getAllAchievementsOfType(type: AchievementType): List<AchievementResponse> {
        val achievements = achievementRepository.getAllActivatedOfType(type)
        val sortedAchievements = achievements.sortedBy { it.targetValue }
        return sortedAchievements.map { AchievementResponse.from(it) }
    }
}