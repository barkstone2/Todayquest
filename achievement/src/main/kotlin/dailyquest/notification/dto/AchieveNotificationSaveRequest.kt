package dailyquest.notification.dto

import dailyquest.achievement.entity.Achievement
import dailyquest.achievement.entity.AchievementType
import dailyquest.common.ReflectionUtil
import dailyquest.notification.annotation.NotificationMetadata
import dailyquest.notification.entity.NotificationType

class AchieveNotificationSaveRequest private constructor(
    override val notificationType: NotificationType,
    override val userId: Long,
    @NotificationMetadata
    val achievementId: Long,
    val achievementTitle: String,
    @NotificationMetadata
    val achievementType: AchievementType,
    val targetValue: Long,
): NotificationSaveRequest {

    override fun createNotificationContent(): String {
        return "${achievementType.representationFormat.format(targetValue)}(으)로 [$achievementTitle] 업적을 달성했습니다."
    }

    override fun createNotificationMetadata(): Map<String, Any> {
        val metadataMap = ReflectionUtil.getAnnotatedPropertiesMap(this, NotificationMetadata::class)
        return metadataMap
    }

    companion object {
        @JvmStatic
        fun of(userId: Long, achievement: Achievement): AchieveNotificationSaveRequest {
            return AchieveNotificationSaveRequest(
                NotificationType.ACHIEVEMENT_ACHIEVE,
                userId,
                achievement.id,
                achievement.title,
                achievement.type,
                achievement.targetValue
            )
        }
    }
}