package todayquest.user.entity

import org.hibernate.annotations.DynamicInsert
import todayquest.common.BaseTimeEntity
import todayquest.user.dto.UserRequestDto
import java.time.LocalTime
import jakarta.persistence.*

@DynamicInsert
@Entity
@Table(name = "user_info")
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

    var level: Int = 1
        protected set
    var exp: Long = 0
        protected set
    var gold: Long = 0
        protected set

    fun updateNickname(nickname: String) {
        this.nickname = nickname
    }

    fun changeUserSettings(dto: UserRequestDto) {
        resetTime = LocalTime.of(dto.resetTime, 0, 0)
        coreTime = LocalTime.of(dto.coreTime, 0, 0)
    }

    fun earnExpAndGold(exp: Long, gold: Long, targetExp: Long) {
        this.gold += gold
        this.exp += exp
        levelUpCheck(targetExp)
    }

    private fun levelUpCheck(targetExp: Long) {
        if (level == 100) return
        if (exp >= targetExp) {
            level += 1
            exp -= targetExp
        }
    }

    fun isNowCoreTime() : Boolean {
        val now = LocalTime.now()
        if (coreTime.isAfter(now) || now.isAfter(coreTime.plusHours(1))) return false;
        return true
    }

}