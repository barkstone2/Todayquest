package dailyquest.user.entity

import dailyquest.common.BaseTimeEntity
import jakarta.persistence.*
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
}