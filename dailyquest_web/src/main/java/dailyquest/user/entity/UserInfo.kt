package dailyquest.user.entity

import dailyquest.common.BaseTimeEntity
import dailyquest.quest.entity.QuestType
import dailyquest.user.dto.RoleType
import jakarta.persistence.*
import org.hibernate.annotations.DynamicInsert
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@DynamicInsert
@Entity
@Table(name = "user_info", uniqueConstraints = [UniqueConstraint(name = "unique_nickname", columnNames = ["nickname"])])
class UserInfo(
    oauth2Id: String,
    nickname: String,
    providerType: ProviderType,
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
    var resetTime: LocalTime = LocalTime.of(6,0,0)
        protected set

    @Column(nullable = false)
    var coreTime: LocalTime = LocalTime.of(8, 0, 0)
        protected set

    var resetTimeLastModifiedDate: LocalDateTime? = null
        protected set
    var coreTimeLastModifiedDate: LocalDateTime? = null
        protected set
    var exp: Long = 0
        protected set
    var gold: Long = 0
        protected set

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var role: RoleType = RoleType.USER

    fun updateNickname(nickname: String?) {
        if (nickname != null) {
            this.nickname = nickname
        }
    }

    fun updateResetTime(resetTime: Int?, requestedDate: LocalDateTime): Boolean {
        if(resetTime == null || resetTime == getResetHour()) return true

        if(canUpdateTimeSetting(resetTimeLastModifiedDate, requestedDate)) {
            this.resetTime = LocalTime.of(resetTime, 0, 0)
            resetTimeLastModifiedDate = requestedDate
            return true
        }

        return false
    }

    fun updateCoreTime(coreTime: Int?, requestedDate: LocalDateTime): Boolean {
        if(coreTime == null || coreTime == getCoreHour()) return true

        if(canUpdateTimeSetting(coreTimeLastModifiedDate, requestedDate)) {
            this.coreTime = LocalTime.of(coreTime, 0, 0)
            coreTimeLastModifiedDate = requestedDate
            return true
        }

        return false
    }

    fun updateExpAndGold(questType: QuestType, earnedExp: Long, earnedGold: Long) {

        val multiplier = when (questType) {
            QuestType.MAIN -> 2
            else -> 1
        }

        this.gold += earnedGold * multiplier
        this.exp += earnedExp * multiplier
    }

    fun isNowCoreTime() : Boolean {
        val now = LocalDateTime.now()
        val coreTimeOfToday = LocalDateTime.of(LocalDate.now(), coreTime)
        if (coreTimeOfToday.isAfter(now) || now.isAfter(coreTimeOfToday.plusHours(1))) return false;
        return true
    }

    fun calculateLevel(expTable: Map<Int, Long>): Triple<Int, Long, Long> {
        var level = 1
        var remainingExp = exp
        var requiredExp = 0L

        expTable.keys.sorted().forEach { key ->
            requiredExp = expTable[key] ?: return@forEach

            if (requiredExp == 0L) return@forEach

            if (remainingExp >= requiredExp) {
                remainingExp -= requiredExp
                level++
            } else {
                return Triple(level, remainingExp, requiredExp)
            }
        }

        return Triple(level, remainingExp, requiredExp)
    }

    fun getResetHour(): Int {
        return resetTime.hour
    }

    fun getCoreHour(): Int {
        return coreTime.hour
    }

    fun getRemainTimeUntilCoreTimeUpdateAvailable(requestedDate: LocalDateTime): Duration {
        val oneDayAfter = coreTimeLastModifiedDate?.plusDays(1L)
        return Duration.between(requestedDate, oneDayAfter)
    }

    fun getRemainTimeUntilResetTimeUpdateAvailable(requestedDate: LocalDateTime): Duration {
        val oneDayAfter = resetTimeLastModifiedDate?.plusDays(1L)
        return Duration.between(requestedDate, oneDayAfter)
    }

    private fun canUpdateTimeSetting(settingLastModifiedDate: LocalDateTime?, requestedDate: LocalDateTime): Boolean {
        if(settingLastModifiedDate == null) return true

        val oneDayAfter = settingLastModifiedDate.plusDays(1L)
        return requestedDate.isAfter(oneDayAfter)
    }

}