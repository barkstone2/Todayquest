package todayquest.user.entity

import jakarta.persistence.*
import org.hibernate.annotations.DynamicInsert
import todayquest.common.BaseTimeEntity
import todayquest.common.MessageUtil
import todayquest.quest.entity.QuestType
import todayquest.user.dto.RoleType
import todayquest.user.dto.UserRequestDto
import java.time.*

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

    fun updateNickname(nickname: String) {
        this.nickname = nickname
    }

    fun changeUserSettings(dto: UserRequestDto) {

        val now = LocalDateTime.now().withSecond(0).withNano(0)
        val compareDate = now.minusDays(1)

        checkUpdateLimit(resetTimeLastModifiedDate, compareDate, "user.settings.reset_time")
        checkUpdateLimit(coreTimeLastModifiedDate, compareDate, "user.settings.core_time")

        if(dto.resetTime != null && dto.resetTime != resetTime.hour) {
            resetTime = LocalTime.of(dto.resetTime, 0, 0)
            resetTimeLastModifiedDate = now
        }

        if(dto.coreTime != null && dto.coreTime != coreTime.hour) {
            coreTime = LocalTime.of(dto.coreTime, 0, 0)
            coreTimeLastModifiedDate = now
        }

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
        val now = LocalTime.now()
        if (coreTime.isAfter(now) || now.isAfter(coreTime.plusHours(1))) return false;
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

    private fun checkUpdateLimit(settingLastModifiedDate: LocalDateTime?, compareDate: LocalDateTime, messageKey: String) {
        check(settingLastModifiedDate?.isBefore(compareDate) ?: true) {
            val diff = Duration.between(compareDate, settingLastModifiedDate)
            val diffStr = String.format("%d시간 %d분", diff.toHours(), diff.toMinutes() % 60)

            MessageUtil.getMessage("user.settings.update_limit", MessageUtil.getMessage(messageKey), diffStr)
        }
    }
}