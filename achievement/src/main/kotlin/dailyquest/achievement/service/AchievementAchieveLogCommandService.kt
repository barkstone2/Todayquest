package dailyquest.achievement.service

import dailyquest.achievement.entity.AchievementAchieveLog
import dailyquest.achievement.repository.AchievementAchieveLogRepository
import dailyquest.achievement.repository.AchievementRepository
import dailyquest.notification.dto.AchieveNotificationSaveRequest
import dailyquest.notification.service.NotificationService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Transactional
@Service
class AchievementAchieveLogCommandService(
    private val achieveLogRepository: AchievementAchieveLogRepository,
    private val achievementRepository: AchievementRepository,
    private val notificationService: NotificationService
) {
    fun saveAchieveLog(achievementId: Long, userId: Long) {
        val achievedAchievement = achievementRepository.getReferenceById(achievementId)
        val achieveLog = AchievementAchieveLog.of(achievedAchievement, userId)
        achieveLogRepository.save(achieveLog)

        val notificationSaveRequest = AchieveNotificationSaveRequest.of(userId, achievedAchievement)
        notificationService.saveNotification(notificationSaveRequest, userId)
    }
}