package dailyquest.achievement.service

import dailyquest.achievement.entity.Achievement
import dailyquest.achievement.entity.AchievementLog
import dailyquest.achievement.repository.AchievementLogRepository
import dailyquest.user.repository.UserRepository
import org.springframework.stereotype.Service

@Service
class AchievementLogService(
    val achievementLogRepository: AchievementLogRepository,
    val userRepository: UserRepository,
) {

    fun achieve(achievableAchievement: Achievement, userId: Long) {
        val user = userRepository.findById(userId).get()
        val achievementLog = AchievementLog.of(achievableAchievement, user)
        achievementLogRepository.save(achievementLog)
    }
}