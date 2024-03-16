package dailyquest.achievement.service

import dailyquest.achievement.entity.Achievement
import dailyquest.achievement.entity.AchievementAchieveLog
import dailyquest.achievement.repository.AchievementAchieveLogRepository
//import dailyquest.notification.dto.AchieveNotificationSaveRequest
//import dailyquest.notification.service.NotificationService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Transactional
@Service
class AchievementAchieveLogCommandService(
    private val achieveLogRepository: AchievementAchieveLogRepository,
//    private val notificationService: NotificationService
) {

    fun achieve(achievableAchievement: Achievement, userId: Long) {
        val achieveLog = AchievementAchieveLog.of(achievableAchievement, userId)
        achieveLogRepository.save(achieveLog)

//        val notificationSaveRequest = AchieveNotificationSaveRequest.of(userId, achievableAchievement)
//        notificationService.saveNotification(notificationSaveRequest, userId)
    }
}