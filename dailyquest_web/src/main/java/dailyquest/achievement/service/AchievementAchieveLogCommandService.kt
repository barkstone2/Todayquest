package dailyquest.achievement.service

import dailyquest.achievement.entity.Achievement
import dailyquest.achievement.entity.AchievementAchieveLog
import dailyquest.achievement.repository.AchievementAchieveLogRepository
import dailyquest.user.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Transactional
@Service
class AchievementAchieveLogCommandService(
    private val achieveLogRepository: AchievementAchieveLogRepository,
    private val userRepository: UserRepository,
) {

    fun achieve(achievableAchievement: Achievement, userId: Long) {
        val user = userRepository.findById(userId).get()
        val achieveLog = AchievementAchieveLog.of(achievableAchievement, user)
        achieveLogRepository.save(achieveLog)
    }
}