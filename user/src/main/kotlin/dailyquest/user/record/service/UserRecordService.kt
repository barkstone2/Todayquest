package dailyquest.user.record.service

import dailyquest.achievement.dto.SimpleAchievementAchieveRequest
import dailyquest.achievement.entity.AchievementType.*
import dailyquest.achievement.service.AchievementService
import dailyquest.user.dto.UserUpdateRequest
import dailyquest.user.record.entity.UserRecord
import dailyquest.user.record.repository.UserRecordRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Transactional(readOnly = true)
@Service
class UserRecordService(
    private val userRecordRepository: UserRecordRepository,
    private val achievementService: AchievementService,
) {
    @Transactional
    fun saveNewRecordEntity(userId: Long) {
        val userRecord = UserRecord(userId)
        userRecordRepository.save(userRecord)
    }

    @Transactional
    fun recordGoldEarn(userId: Long, updateRequest: UserUpdateRequest) {
        val userRecord = userRecordRepository.findByIdOrNull(userId)!!
        userRecord.recordGoldEarn(updateRequest.earnedGold)
        val goldEarnAchieveRequest = SimpleAchievementAchieveRequest.of(GOLD_EARN, userId, userRecord.goldEarnAmount)
        achievementService.checkAndAchieveAchievement(goldEarnAchieveRequest)
    }

    @Transactional
    fun recordQuestRegistration(userId: Long, registrationDate: LocalDate) {
        val userRecord = userRecordRepository.findByIdOrNull(userId)!!
        userRecord.increaseQuestRegistrationCount(registrationDate)
        val questRegAchieveRequest = SimpleAchievementAchieveRequest.of(QUEST_REGISTRATION, userId, userRecord.questRegistrationCount)
        achievementService.checkAndAchieveAchievement(questRegAchieveRequest)
        val questContRegAchieveRequest = SimpleAchievementAchieveRequest.of(QUEST_CONTINUOUS_REGISTRATION, userId, userRecord.currentQuestContinuousRegistrationDays)
        achievementService.checkAndAchieveAchievement(questContRegAchieveRequest)
    }

    @Transactional
    fun recordQuestCompletion(userId: Long, completionDate: LocalDate) {
        val userRecord = userRecordRepository.findByIdOrNull(userId)!!
        userRecord.increaseQuestCompletionCount(completionDate)
        val questCompAchieveRequest = SimpleAchievementAchieveRequest.of(QUEST_COMPLETION, userId, userRecord.questCompletionCount)
        achievementService.checkAndAchieveAchievement(questCompAchieveRequest)
        val questContCompAchieveRequest = SimpleAchievementAchieveRequest.of(QUEST_CONTINUOUS_COMPLETION, userId, userRecord.currentQuestContinuousCompletionDays)
        achievementService.checkAndAchieveAchievement(questContCompAchieveRequest)
    }
}