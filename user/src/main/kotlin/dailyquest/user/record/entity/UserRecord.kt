package dailyquest.user.record.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDate
import kotlin.math.max

@Table(name = "user_record")
@Entity
class UserRecord @JvmOverloads constructor(
    id: Long,
    questRegistrationCount: Long = 0,
    questCompletionCount: Long = 0,
    currentQuestContinuousRegistrationDays: Long = 0,
    currentQuestContinuousCompletionDays: Long = 0,
    maxQuestContinuousRegistrationDays: Long = 0,
    maxQuestContinuousCompletionDays: Long = 0,
    lastQuestRegistrationDate: LocalDate? = null,
    lastQuestCompletionDate: LocalDate? = null,
    perfectDayCount: Long = 0,
    goldEarnAmount: Long = 0,
    goldUseAmount: Long = 0,
) {
    @Column(name = "user_id")
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = id

    @Column(name = "quest_registration_count", nullable = false)
    var questRegistrationCount: Long = questRegistrationCount
        protected set

    @Column(name = "quest_completion_count", nullable = false)
    var questCompletionCount: Long = questCompletionCount
        protected set

    @Column(name = "current_quest_continuous_registration_days", nullable = false)
    var currentQuestContinuousRegistrationDays: Long = currentQuestContinuousRegistrationDays
        protected set

    @Column(name = "current_quest_continuous_completion_days", nullable = false)
    var currentQuestContinuousCompletionDays: Long = currentQuestContinuousCompletionDays
        protected set

    @Column(name = "max_quest_continuous_registration_days", nullable = false)
    var maxQuestContinuousRegistrationDays: Long = maxQuestContinuousRegistrationDays
        protected set

    @Column(name = "max_quest_continuous_completion_days", nullable = false)
    var maxQuestContinuousCompletionDays: Long = maxQuestContinuousCompletionDays
        protected set

    @Column(name = "last_quest_registration_date")
    var lastQuestRegistrationDate: LocalDate? = lastQuestRegistrationDate
        protected set

    @Column(name = "last_quest_completion_date")
    var lastQuestCompletionDate: LocalDate? = lastQuestCompletionDate
        protected set

    @Column(name = "perfect_day_count", nullable = false)
    var perfectDayCount: Long = perfectDayCount
        protected set

    @Column(name = "gold_earn_amount", nullable = false)
    var goldEarnAmount: Long = goldEarnAmount
        protected set

    @Column(name = "gold_use_amount", nullable = false)
    var goldUseAmount: Long = goldUseAmount
        protected set

    fun recordGoldEarn(goldEarnAmount: Long) {
        this.goldEarnAmount += goldEarnAmount
    }

    fun recordGoldUse(goldUseAmount: Long) {
        this.goldUseAmount += goldUseAmount
    }

    fun increaseQuestRegistrationCount(registrationDate: LocalDate) {
        this.questRegistrationCount++
        if (this.isContinuousRegistration(registrationDate)) {
            this.increaseCurrentQuestContinuousRegistrationDays()
        } else if (registrationDate != lastQuestRegistrationDate) {
            this.currentQuestContinuousRegistrationDays = 1
        }
        if (this.isLatestRegistration(registrationDate)) {
            this.lastQuestRegistrationDate = registrationDate
        }
    }

    private fun isContinuousRegistration(registrationDate: LocalDate) =
        lastQuestRegistrationDate == null || lastQuestRegistrationDate!!.isEqual(registrationDate.minusDays(1))

    private fun increaseCurrentQuestContinuousRegistrationDays() {
        this.currentQuestContinuousRegistrationDays++
        this.maxQuestContinuousRegistrationDays = max(maxQuestContinuousRegistrationDays, currentQuestContinuousRegistrationDays)
    }

    private fun isLatestRegistration(registrationDate: LocalDate): Boolean =
        this.lastQuestRegistrationDate?.isBefore(registrationDate) != false

    fun increaseQuestCompletionCount(completionDate: LocalDate) {
        this.questCompletionCount++
        if (this.isContinuousCompletion(completionDate)) {
            this.increaseCurrentQuestContinuousCompletionDays()
        } else if (completionDate != lastQuestCompletionDate) {
            this.currentQuestContinuousCompletionDays = 1
        }
        if (this.isLatestCompletion(completionDate)) {
            this.lastQuestCompletionDate = completionDate
        }
    }

    private fun isContinuousCompletion(completionDate: LocalDate) =
        lastQuestCompletionDate == null || lastQuestCompletionDate!!.isEqual(completionDate.minusDays(1))

    private fun increaseCurrentQuestContinuousCompletionDays() {
        this.currentQuestContinuousCompletionDays++
        this.maxQuestContinuousCompletionDays = max(maxQuestContinuousCompletionDays, currentQuestContinuousCompletionDays)
    }

    private fun isLatestCompletion(completionDate: LocalDate): Boolean =
        this.lastQuestCompletionDate?.isBefore(completionDate) != false

    fun increasePerfectDayCount() {
        this.perfectDayCount++
    }
}