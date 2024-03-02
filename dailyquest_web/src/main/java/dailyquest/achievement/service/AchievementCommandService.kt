package dailyquest.achievement.service

import dailyquest.achievement.dto.AchievementAchieveRequest
import dailyquest.achievement.dto.AchievementRequest
import dailyquest.achievement.repository.AchievementRepository
import dailyquest.achievement.util.AchievementCurrentValueResolver
import dailyquest.common.MessageUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.IllegalStateException

@Transactional
@Service
class AchievementCommandService @Autowired constructor(
    private val achievementQueryService: AchievementQueryService,
    private val achievementLogCommandService: AchievementLogCommandService,
    private val achievementCurrentValueResolver: AchievementCurrentValueResolver,
    private val achievementRepository: AchievementRepository,
) {

    @Async
    fun checkAndAchieveAchievement(achieveRequest: AchievementAchieveRequest) {
        val targetAchievement = achievementQueryService.getNotAchievedAchievement(achieveRequest.type, achieveRequest.userId)
        val currentValue = achievementCurrentValueResolver.resolveCurrentValue(achieveRequest, targetAchievement)
        if (targetAchievement.canAchieve(currentValue)) {
            achievementLogCommandService.achieve(targetAchievement, achieveRequest.userId)
        }
    }

    @Throws(IllegalStateException::class)
    fun saveAchievement(saveRequest: AchievementRequest): Long {
        val isDuplicated = achievementRepository.existsByTypeAndTargetValue(saveRequest.type, saveRequest.targetValue)
        if (isDuplicated) {
            val errorMessage = MessageUtil.getMessage("achievement.duplicated")
            throw IllegalStateException(errorMessage)
        }
        val saveEntity = saveRequest.mapToEntity()
        achievementRepository.save(saveEntity)
        return saveEntity.id
    }
}