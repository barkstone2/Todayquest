package dailyquest.notification.dto

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
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
    val targetValue: Int,
): NotificationSaveRequest {

    override fun createNotificationContent(): String {
        return "${achievementType.representationFormat.format(targetValue)}(으)로 $achievementTitle 업적을 달성했습니다."
    }

    override fun createNotificationMetadata(): Map<String, String> {
        val metadataMap = mutableMapOf<String, String>()
        val annotatedProperties = ReflectionUtil.getAnnotatedProperties(AchieveNotificationSaveRequest::class, NotificationMetadata::class)
        annotatedProperties.forEach { metadataMap[it.name] = it.get(this).toString() }
        return metadataMap
    }

    override fun createNotificationMetadataJson(): String {
        val metadataMap = createNotificationMetadata()
        return objectMapper.writeValueAsString(metadataMap)
    }

    companion object {
        @JvmStatic
        private val objectMapper: ObjectMapper = jacksonObjectMapper().registerKotlinModule()

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