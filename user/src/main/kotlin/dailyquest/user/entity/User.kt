package dailyquest.user.entity

import dailyquest.common.BaseTimeEntity
import dailyquest.user.dto.UserUpdateRequest
import jakarta.persistence.*
import java.time.LocalDateTime
import java.time.LocalTime

@Entity
@Table(name = "users", uniqueConstraints = [UniqueConstraint(name = "unique_nickname", columnNames = ["nickname"])])
class User @JvmOverloads constructor(
    oauth2Id: String,
    nickname: String,
    providerType: ProviderType,
    coreTime: LocalTime = LocalTime.of(8, 0, 0),
    coreTimeLastModifiedDate: LocalDateTime? = null,
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
    }

    fun getCoreHour(): Int {
        return coreTime.hour
    }
}