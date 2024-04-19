package dailyquest.user.service

import dailyquest.achievement.dto.AchievementAchieveRequest
import dailyquest.achievement.entity.AchievementType.*
import dailyquest.achievement.service.AchievementService
import dailyquest.common.timeSinceNowAsString
import dailyquest.user.dto.UserResponse
import dailyquest.user.dto.UserSaveRequest
import dailyquest.user.dto.UserUpdateRequest
import dailyquest.user.entity.User
import dailyquest.user.repository.UserRepository
import jakarta.persistence.EntityNotFoundException
import org.springframework.context.MessageSource
import org.springframework.context.support.MessageSourceAccessor
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.jvm.optionals.getOrNull

@Transactional(readOnly = true)
@Service
class UserService(
    private val userRepository: UserRepository,
    private val achievementService: AchievementService,
    messageSource: MessageSource
) {
    private val messageSourceAccessor: MessageSourceAccessor = MessageSourceAccessor(messageSource)

    fun findUserByOauthId(oauth2Id: String): UserResponse? {
        return userRepository.findByOauth2Id(oauth2Id)?.let { UserResponse.from(it) }
    }

    fun getUserById(userId: Long): UserResponse {
        return UserResponse.from(this.findUser(userId))
    }

    @Throws(EntityNotFoundException::class)
    private fun findUser(userId: Long): User {
        val foundUser = userRepository.findById(userId).getOrNull()
        val entityNotFoundMessage = messageSourceAccessor.getMessage("exception.entity.notfound", messageSourceAccessor.getMessage("user"))
        return foundUser ?: throw EntityNotFoundException(entityNotFoundMessage)
    }

    fun isDuplicatedNickname(nickname: String): Boolean {
        return userRepository.existsByNickname(nickname)
    }

    @Transactional
    fun saveUser(saveRequest: UserSaveRequest): Long  {
        val requestEntity = saveRequest.mapToEntity()
        return userRepository.save(requestEntity).id
    }

    @Transactional
    @Throws(IllegalStateException::class)
    fun updateUser(userId: Long, updateRequest: UserUpdateRequest) {
        val updateTarget = this.findUser(userId)
        val updateSucceed = updateTarget.updateUser(updateRequest)
        if (!updateSucceed) {
            val updateAvailableTime: LocalDateTime = updateTarget.getUpdateAvailableDateTimeOfCoreTime()
            val timeSinceNowUntilAvailable: String = updateAvailableTime.timeSinceNowAsString()
            val errorMessage: String = messageSourceAccessor.getMessage("user.coreTime.updateLimit", timeSinceNowUntilAvailable)
            throw IllegalStateException(errorMessage)
        }
    }

    @Transactional
    fun addUserExpAndGold(userId: Long, updateRequest: UserUpdateRequest) {
        val updateTarget = this.findUser(userId)
        updateTarget.addExpAndGold(updateRequest.earnedExp, updateRequest.earnedGold)
        val goldEarnAchieveRequest = AchievementAchieveRequest.of(GOLD_EARN, userId, updateTarget.goldEarnAmount)
        achievementService.checkAndAchieveAchievement(goldEarnAchieveRequest)
    }

    @Transactional
    fun recordQuestRegistration(userId: Long, registrationDate: LocalDate) {
        val targetUser = this.findUser(userId)
        targetUser.increaseQuestRegistrationCount(registrationDate)
        val questRegAchieveRequest = AchievementAchieveRequest.of(QUEST_REGISTRATION, userId, targetUser.questRegistrationCount)
        achievementService.checkAndAchieveAchievement(questRegAchieveRequest)
        val questContRegAchieveRequest = AchievementAchieveRequest.of(QUEST_CONTINUOUS_REGISTRATION, userId, targetUser.currentQuestContinuousRegistrationDays)
        achievementService.checkAndAchieveAchievement(questContRegAchieveRequest)
    }

    @Transactional
    fun recordQuestCompletion(userId: Long, completionDate: LocalDate) {
        val targetUser = this.findUser(userId)
        targetUser.increaseQuestCompletionCount(completionDate)
        val questCompAchieveRequest = AchievementAchieveRequest.of(QUEST_COMPLETION, userId, targetUser.questCompletionCount)
        achievementService.checkAndAchieveAchievement(questCompAchieveRequest)
        val questContCompAchieveRequest = AchievementAchieveRequest.of(QUEST_CONTINUOUS_COMPLETION, userId, targetUser.currentQuestContinuousCompletionDays)
        achievementService.checkAndAchieveAchievement(questContCompAchieveRequest)
    }
}
