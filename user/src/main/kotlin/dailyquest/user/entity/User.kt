package dailyquest.user.entity

import dailyquest.common.BaseTimeEntity
import dailyquest.user.dto.UserUpdateRequest
import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import kotlin.math.max

@Entity
@Table(name = "users", uniqueConstraints = [UniqueConstraint(name = "unique_nickname", columnNames = ["nickname"])])
class User @JvmOverloads constructor(
    oauth2Id: String,
    nickname: String,
    providerType: ProviderType,
    coreTime: LocalTime = LocalTime.of(8, 0, 0),
    coreTimeLastModifiedDate: LocalDateTime? = null,
    questRegistrationCount: Int = 0,
    questCompletionCount: Int = 0,
    currentQuestContinuousRegistrationDays: Int = 0,
    currentQuestContinuousCompletionDays: Int = 0,
    maxQuestContinuousRegistrationDays: Int = 0,
    maxQuestContinuousCompletionDays: Int = 0,
    lastQuestRegistrationDate: LocalDate? = null,
    lastQuestCompletionDate: LocalDate? = null,
    perfectDayCount: Int = 0,
    goldEarnAmount: Long = 0,
    goldUseAmount: Long = 0,
) : BaseTimeEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    val id: Long = 0

    @Column(nullable = false)
    val oauth2Id: String = oauth2Id

    @Column(nullable = false, length = 20)
    var nickname: String = nickname
        protected set

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val providerType: ProviderType = providerType

    @Column(nullable = false)
    var coreTime: LocalTime = coreTime
        protected set

    @Column(name = "core_time_last_modified_date")
    var coreTimeLastModifiedDate: LocalDateTime? = coreTimeLastModifiedDate
        protected set

    @Column(nullable = false)
    var exp: Long = 0
        protected set

    @Column(nullable = false)
    var gold: Long = 0
        protected set

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var role: RoleType = RoleType.USER

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
    var goldEarnAmount: Long = goldEarnAmount
        protected set

    @Column(name = "gold_use_amount", nullable = false)
    var goldUseAmount: Long = goldUseAmount
        protected set

    fun updateUser(updateRequest: UserUpdateRequest): Boolean {
        if (this.updateCoreTime(updateRequest.coreTime)) {
            this.updateNickname(updateRequest.nickname)
            return true
        }
        return false
    }

    fun updateNickname(nickname: String?) {
        if (nickname != null) {
            this.nickname = nickname
        }
    }

    fun updateCoreTime(coreTime: Int?): Boolean {
        if (this.isSameOrNullCoreTime(coreTime)) return true
        if (this.canUpdateCoreTime()) {
            this.coreTime = LocalTime.of(coreTime!!, 0, 0)
            coreTimeLastModifiedDate = LocalDateTime.now()
            return true
        }
        return false
    }

    private fun isSameOrNullCoreTime(coreTime: Int?) = coreTime == null || coreTime == getCoreHour()

    private fun canUpdateCoreTime(): Boolean {
        if (this.coreTimeLastModifiedDate == null) return true
        val updateAvailableDateTime = this.getUpdateAvailableDateTimeOfCoreTime()
        val now = LocalDateTime.now()
        return now.isAfter(updateAvailableDateTime)
    }

    fun getUpdateAvailableDateTimeOfCoreTime(): LocalDateTime {
        return coreTimeLastModifiedDate?.plusDays(1L) ?: LocalDateTime.now()
    }

    fun addExpAndGold(earnedExp: Long, earnedGold: Long) {
        this.exp += earnedExp
        this.gold += earnedGold
        this.goldEarnAmount += earnedGold
    }

    fun isNowCoreTime() : Boolean {
        val now = LocalDateTime.now()
        val coreTimeOfToday = LocalDateTime.of(LocalDate.now(), coreTime)
        if (coreTimeOfToday.isAfter(now) || now.isAfter(coreTimeOfToday.plusHours(1))) return false;
        return true
    }

    fun getCoreHour(): Int {
        return coreTime.hour
    }

    fun increaseQuestRegistrationCount(registrationDate: LocalDate) {
        this.questRegistrationCount++
        if (this.isContinuousRegistration(registrationDate)) {
            this.increaseCurrentQuestContinuousRegistrationDays()
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

    fun addGoldUseAmount(goldUseAmount: Int) {
        this.goldUseAmount += goldUseAmount
    }
}