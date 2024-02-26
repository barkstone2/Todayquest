package dailyquest.user.entity

import dailyquest.common.BaseTimeEntity
import dailyquest.quest.entity.QuestType
import jakarta.persistence.*
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

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
    var coreTime: LocalTime = LocalTime.of(8, 0, 0)
        protected set

    var coreTimeLastModifiedDate: LocalDateTime? = null
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

    fun updateNickname(nickname: String?) {
        if (nickname != null) {
            this.nickname = nickname
        }
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

    fun updateExpAndGold(earnedExp: Long, earnedGold: Long) {
        this.gold += earnedGold
        this.exp += earnedExp
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

    fun getCoreHour(): Int {
        return coreTime.hour
    }

    fun getRemainTimeUntilCoreTimeUpdateAvailable(requestedDate: LocalDateTime): Duration {
        val oneDayAfter = coreTimeLastModifiedDate?.plusDays(1L)
        return Duration.between(requestedDate, oneDayAfter)
    }

    private fun canUpdateTimeSetting(settingLastModifiedDate: LocalDateTime?, requestedDate: LocalDateTime): Boolean {
        if(settingLastModifiedDate == null) return true

        val oneDayAfter = settingLastModifiedDate.plusDays(1L)
        return requestedDate.isAfter(oneDayAfter)
    }

}