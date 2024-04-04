package dailyquest.achievement.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDate
import kotlin.math.max

@Table(name = "achievement_current_value")
@Entity
class AchievementCurrentValue(
    userId: Long,
    questRegistrationCount: Int = 0,
    questCompletionCount: Int = 0,
    currentQuestContinuousRegistrationDays: Int = 0,
    currentQuestContinuousCompletionDays: Int = 0,
    maxQuestContinuousRegistrationDays: Int = 0,
    maxQuestContinuousCompletionDays: Int = 0,
    lastQuestRegistrationDate: LocalDate? = null,
    lastQuestCompletionDate: LocalDate? = null,
    perfectDayCount: Int = 0,
    goldEarnAmount: Int = 0,
    goldUseAmount: Int = 0,
) {
    @Id
    @Column(name = "user_id", nullable = false)
    val id: Long = userId

    @Column(name = "quest_registration_count", nullable = false)
    var questRegistrationCount: Int = questRegistrationCount
        protected set

    @Column(name = "quest_completion_count", nullable = false)
    var questCompletionCount: Int = questCompletionCount
        protected set

    @Column(name = "current_quest_continuous_registration_days", nullable = false)
    var currentQuestContinuousRegistrationDays: Int = currentQuestContinuousRegistrationDays
        protected set

    @Column(name = "current_quest_continuous_completion_days", nullable = false)
    var currentQuestContinuousCompletionDays: Int = currentQuestContinuousCompletionDays
        protected set

    @Column(name = "max_quest_continuous_registration_days", nullable = false)
    var maxQuestContinuousRegistrationDays: Int = maxQuestContinuousRegistrationDays
        protected set

    @Column(name = "max_quest_continuous_completion_days", nullable = false)
    var maxQuestContinuousCompletionDays: Int = maxQuestContinuousCompletionDays
        protected set

    @Column(name = "last_quest_registration_date")
    var lastQuestRegistrationDate: LocalDate? = lastQuestRegistrationDate
        protected set

    @Column(name = "last_quest_completion_date")
    var lastQuestCompletionDate: LocalDate? = lastQuestCompletionDate
        protected set

    @Column(name = "perfect_day_count", nullable = false)
    var perfectDayCount: Int = perfectDayCount
        protected set

    @Column(name = "gold_earn_amount", nullable = false)
    var goldEarnAmount: Int = goldEarnAmount
        protected set

    @Column(name = "gold_use_amount", nullable = false)
    var goldUseAmount: Int = goldUseAmount
        protected set

    fun increaseQuestRegistrationCount(registrationDate: LocalDate) {
        this.questRegistrationCount++
        if (this.isContinuousRegistration(registrationDate)) {
            this.increaseCurrentQuestContinuousRegistrationDays()
        }
    }

    private fun isContinuousRegistration(registrationDate: LocalDate) =
        lastQuestRegistrationDate == null || lastQuestRegistrationDate!!.isEqual(registrationDate.minusDays(1))

    fun increaseQuestCompletionCount(completionDate: LocalDate) {
        this.questCompletionCount++
        if (this.isContinuousCompletion(completionDate)) {
            this.increaseCurrentQuestContinuousCompletionDays()
        }
    }

    private fun isContinuousCompletion(completionDate: LocalDate) =
        lastQuestCompletionDate == null || lastQuestCompletionDate!!.isEqual(completionDate.minusDays(1))

    fun increaseCurrentQuestContinuousRegistrationDays() {
        this.currentQuestContinuousRegistrationDays++
        this.maxQuestContinuousRegistrationDays = max(maxQuestContinuousRegistrationDays, currentQuestContinuousRegistrationDays)
    }

    fun increaseCurrentQuestContinuousCompletionDays() {
        this.currentQuestContinuousCompletionDays++
        this.maxQuestContinuousCompletionDays = max(maxQuestContinuousCompletionDays, currentQuestContinuousCompletionDays)
    }

    fun increasePerfectDayCount() {
        this.perfectDayCount++
    }

    fun addGoldEarnAmount(goldEarnAmount: Int) {
        this.goldEarnAmount += goldEarnAmount
    }

    fun addGoldUseAmount(goldUseAmount: Int) {
        this.goldUseAmount += goldUseAmount
    }

    fun getValueForType(achievementType: AchievementType): Int {
        return when (achievementType) {
            AchievementType.QUEST_REGISTRATION -> questRegistrationCount
            AchievementType.QUEST_COMPLETION -> questCompletionCount
            AchievementType.QUEST_CONTINUOUS_REGISTRATION_DAYS -> currentQuestContinuousRegistrationDays
            AchievementType.QUEST_CONTINUOUS_COMPLETION -> currentQuestContinuousCompletionDays
            AchievementType.USER_LEVEL -> 0
            AchievementType.GOLD_EARN -> goldEarnAmount
            AchievementType.PERFECT_DAY -> perfectDayCount
            AchievementType.EMPTY -> 0
        }
    }
}