package dailyquest.achievement.service

import dailyquest.achievement.entity.AchievementCurrentValue
import dailyquest.achievement.repository.AchievementCurrentValueRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Transactional
@Service
class AchievementCurrentValueCommandService(
    private val achievementCurrentValueRepository: AchievementCurrentValueRepository
) {
    fun saveForUser(userId: Long) {
        val saveEntity = AchievementCurrentValue(userId)
        achievementCurrentValueRepository.save(saveEntity)
    }

    fun recordQuestRegistration(userId: Long, registrationDate: LocalDate) {
        val currentValueOfUser = achievementCurrentValueRepository.findByIdOrNull(userId)
        currentValueOfUser?.increaseQuestRegistrationCount(registrationDate)
    }

    fun recordQuestCompletion(userId: Long, completionDate: LocalDate) {
        val currentValueOfUser = achievementCurrentValueRepository.findByIdOrNull(userId)
        currentValueOfUser?.increaseQuestCompletionCount(completionDate)
    }
}