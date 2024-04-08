package dailyquest.achievement.service

import dailyquest.achievement.dto.AchievementAchieveRequest
import dailyquest.achievement.dto.AchievementSaveRequest
import dailyquest.achievement.dto.AchievementUpdateRequest
import dailyquest.achievement.entity.Achievement
import dailyquest.achievement.repository.AchievementRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.MessageSource
import org.springframework.context.support.MessageSourceAccessor
import org.springframework.data.repository.findByIdOrNull
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Transactional
@Service
class AchievementCommandService @Autowired constructor(
    private val achieveLogCommandService: AchievementAchieveLogCommandService,
    private val achievementRepository: AchievementRepository,
    messageSource: MessageSource
) {
    private val messageSourceAccessor: MessageSourceAccessor = MessageSourceAccessor(messageSource)

    @Async
    fun checkAndAchieveAchievement(achieveRequest: AchievementAchieveRequest) {
        val targetAchievement = this.getNotAchievedAchievement(achieveRequest)
        if (targetAchievement.canAchieve(achieveRequest.currentValue)) {
            achieveLogCommandService.saveAchieveLog(targetAchievement.id, achieveRequest.userId)
        }
    }

    private fun getNotAchievedAchievement(achieveRequest: AchievementAchieveRequest): Achievement {
        return achievementRepository.findNotAchievedAchievement(achieveRequest.type, achieveRequest.userId) ?: Achievement.empty()
    }

    @Throws(IllegalStateException::class)
    fun saveAchievement(saveRequest: AchievementSaveRequest): Long {
        val isDuplicated = achievementRepository.existsByTypeAndTargetValue(saveRequest.type, saveRequest.targetValue)
        if (isDuplicated) {
            val errorMessage = this.getDuplicateErrorMessage()
            throw IllegalStateException(errorMessage)
        }
        val saveEntity = saveRequest.mapToEntity()
        achievementRepository.save(saveEntity)
        return saveEntity.id
    }

    private fun getDuplicateErrorMessage(): String {
        return messageSourceAccessor.getMessage("achievement.duplicated")
    }

    fun updateAchievement(achievementId: Long, updateRequest: AchievementUpdateRequest) {
        val updateTarget = achievementRepository.findByIdOrNull(achievementId)
        updateTarget?.updateAchievement(updateRequest)
    }

    fun inactivateAchievement(achievementId: Long) {
        val updateTarget = achievementRepository.findByIdOrNull(achievementId)
        updateTarget?.inactivateAchievement()
    }

    fun activateAchievement(achievementId: Long) {
        val updateTarget = achievementRepository.findByIdOrNull(achievementId)
        updateTarget?.activateAchievement()
    }
}