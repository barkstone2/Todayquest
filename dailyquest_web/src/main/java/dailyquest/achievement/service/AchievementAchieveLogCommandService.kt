package dailyquest.achievement.service

import dailyquest.achievement.entity.Achievement
import dailyquest.achievement.entity.AchievementAchieveLog
import dailyquest.achievement.repository.AchievementAchieveLogRepository
import dailyquest.notification.dto.AchieveNotificationSaveRequest
import dailyquest.notification.service.NotificationService
import dailyquest.user.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Transactional
@Service
class AchievementAchieveLogCommandService(
    private val achieveLogRepository: AchievementAchieveLogRepository,
    private val userRepository: UserRepository,
    private val notificationService: NotificationService
) {

    fun achieve(achievableAchievement: Achievement, userId: Long) {
        val userReference = userRepository.getReferenceById(userId)
        val achieveLog = AchievementAchieveLog.of(achievableAchievement, userReference)
        achieveLogRepository.save(achieveLog)

        val notificationSaveRequest = AchieveNotificationSaveRequest.of(userId, achievableAchievement)
        notificationService.saveNotification(notificationSaveRequest, userId)
    }
}