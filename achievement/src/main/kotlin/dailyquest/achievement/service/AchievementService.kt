package dailyquest.achievement.service

import dailyquest.achievement.dto.AchievementAchieveRequest
import dailyquest.achievement.dto.AchievementResponse
import dailyquest.achievement.dto.AchievementSaveRequest
import dailyquest.achievement.dto.AchievementUpdateRequest
import dailyquest.achievement.entity.Achievement
import dailyquest.achievement.entity.AchievementType
import dailyquest.achievement.repository.AchievementRepository
import dailyquest.properties.AchievementPageSizeProperties
import org.springframework.context.MessageSource
import org.springframework.context.support.MessageSourceAccessor
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Transactional(readOnly = true)
@Service
class AchievementService(
    private val achievementRepository: AchievementRepository,
    private val achievementPageSizeProperties: AchievementPageSizeProperties,
    private val achieveLogCommandService: AchievementAchieveLogCommandService,
    messageSource: MessageSource
) {
    private val messageSourceAccessor: MessageSourceAccessor = MessageSourceAccessor(messageSource)

    fun getAchievedAchievements(userId: Long, page: Int): Page<AchievementResponse> {
        val pageRequest = PageRequest.of(page, achievementPageSizeProperties.size)
        return achievementRepository.getAchievedAchievements(userId, pageRequest)
    }

    fun getNotAchievedAchievements(userId: Long, page: Int): Page<AchievementResponse> {
        val pageRequest = PageRequest.of(page, achievementPageSizeProperties.size)
        return achievementRepository.getNotAchievedAchievements(userId, pageRequest)
    }

    @Transactional
    @Async
    fun checkAndAchieveAchievement(achieveRequest: AchievementAchieveRequest) {
        val targetAchievement = this.getNotAchievedAchievement(achieveRequest)
        if (targetAchievement?.canAchieve(achieveRequest.currentValue) == true) {
            achieveLogCommandService.saveAchieveLog(targetAchievement.id, achieveRequest.userId)
        }
    }

    private fun getNotAchievedAchievement(achieveRequest: AchievementAchieveRequest): Achievement? {
        return achievementRepository.findNotAchievedAchievement(achieveRequest.type, achieveRequest.userId)
    }

    @Transactional
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

    @Transactional
    fun updateAchievement(achievementId: Long, updateRequest: AchievementUpdateRequest) {
        val updateTarget = achievementRepository.findByIdOrNull(achievementId)
        updateTarget?.updateAchievement(updateRequest)
    }

    @Transactional
    fun inactivateAchievement(achievementId: Long) {
        val updateTarget = achievementRepository.findByIdOrNull(achievementId)
        updateTarget?.inactivateAchievement()
    }

    @Transactional
    fun activateAchievement(achievementId: Long) {
        val updateTarget = achievementRepository.findByIdOrNull(achievementId)
        updateTarget?.activateAchievement()
    }

    fun getAllAchievementsGroupByType(): Map<AchievementType, List<AchievementResponse>> {
        val result = mutableMapOf<AchievementType, MutableList<AchievementResponse>>()
        val achievements = achievementRepository.getAllByOrderByTypeAscTargetValueAsc()
        achievements.groupByTo(result, Achievement::type) { AchievementResponse.from(it) }
        AchievementType.values().forEach {
            result.putIfAbsent(it, mutableListOf())
        }
        return result
    }
}