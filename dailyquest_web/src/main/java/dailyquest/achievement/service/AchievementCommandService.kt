package dailyquest.achievement.service

import dailyquest.achievement.dto.AchievementAchieveRequest
import dailyquest.achievement.dto.AchievementRequest
import dailyquest.achievement.entity.Achievement
import dailyquest.achievement.repository.AchievementRepository
import dailyquest.achievement.util.AchievementCurrentValueResolver
import dailyquest.common.MessageUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Transactional
@Service
class AchievementCommandService @Autowired constructor(
    private val achieveLogCommandService: AchievementAchieveLogCommandService,
    private val achievementCurrentValueResolver: AchievementCurrentValueResolver,
    private val achievementRepository: AchievementRepository,
) {

    @Async
    fun checkAndAchieveAchievement(achieveRequest: AchievementAchieveRequest) {
        val targetAchievement = this.getNotAchievedAchievement(achieveRequest)
        val currentValue = achievementCurrentValueResolver.resolveCurrentValue(achieveRequest, targetAchievement)
        if (targetAchievement.canAchieve(currentValue)) {
            achieveLogCommandService.achieve(targetAchievement, achieveRequest.userId)
        }
    }

    private fun getNotAchievedAchievement(achieveRequest: AchievementAchieveRequest): Achievement {
        return achievementRepository.findNotAchievedAchievement(achieveRequest.type, achieveRequest.userId) ?: Achievement.empty()
    }

    @Throws(IllegalStateException::class)
    fun saveAchievement(saveRequest: AchievementRequest): Long {
        val isDuplicated = achievementRepository.existsByTypeAndTargetValue(saveRequest.type, saveRequest.targetValue)
        if (isDuplicated) {
            val errorMessage = this.getDuplicateErrorMessage()
            throw IllegalStateException(errorMessage)
        }
        val saveEntity = saveRequest.mapToEntity()
        achievementRepository.save(saveEntity)
        return saveEntity.id
    }

    private fun getDuplicateErrorMessage(): String = MessageUtil.getMessage("achievement.duplicated")
}