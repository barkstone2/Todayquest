package dailyquest.user.dto

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonGetter
import com.fasterxml.jackson.annotation.JsonIgnore
import dailyquest.user.entity.ProviderType
import dailyquest.user.entity.RoleType
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class UserPrincipal(
    val id: Long,
    val nickname: String,
    val providerType: ProviderType = ProviderType.GOOGLE,
    private val authorities: MutableCollection<GrantedAuthority> = mutableListOf(SimpleGrantedAuthority(RoleType.USER.code)),
    val level: Int = 0,
    val currentExp: Long = 0,
    val requireExp: Long = 0,
    val gold: Long = 0,
    val coreTimeHour: Int = 8,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val coreTimeLastModifiedDate: LocalDateTime? = null,
    val questRegistrationCount: Long = 0,
    val questCompletionCount: Long = 0,
    val currentQuestContinuousRegistrationDays: Long = 0,
    val currentQuestContinuousCompletionDays: Long = 0,
    val maxQuestContinuousRegistrationDays: Long = 0,
    val maxQuestContinuousCompletionDays: Long = 0,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    val lastQuestRegistrationDate: LocalDate? = null,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    val lastQuestCompletionDate: LocalDate? = null,
    val perfectDayCount: Long = 0,
    val goldEarnAmount: Long = 0,
    val goldUseAmount: Long = 0,
) : UserDetails {

    var notificationCount: Int = 0

    @JsonGetter("authorities")
    fun getAuthorityValues(): List<String> {
        return getAuthorities().map { it.authority }
    }

    fun isNowCoreTime() : Boolean {
        val now = LocalDateTime.now()
        val coreTimeOfToday = LocalDateTime.of(LocalDate.now(), LocalTime.of(coreTimeHour, 0))
        val isNowBetweenCoreTimeAndAfterOneHour = !(now.isBefore(coreTimeOfToday) || now.isAfter(coreTimeOfToday.plusHours(1)))
        return isNowBetweenCoreTimeAndAfterOneHour
    }

    @JsonIgnore
    override fun getAuthorities(): MutableCollection<out GrantedAuthority> {
        return authorities
    }

    override fun getPassword(): String {
        return ""
    }

    override fun getUsername(): String {
        return nickname
    }

    override fun isAccountNonExpired(): Boolean {
        return true
    }

    override fun isAccountNonLocked(): Boolean {
        return true
    }

    override fun isCredentialsNonExpired(): Boolean {
        return true
    }

    override fun isEnabled(): Boolean {
        return true
    }

    companion object {
        @JvmStatic
        fun from(userResponse: UserResponse, expTable: Map<Int, Long>): UserPrincipal {
            val (currentLevel, currentExp, requireExp) = userResponse.calculateLevel(expTable)
            return UserPrincipal(
                id = userResponse.id,
                nickname = userResponse.nickname,
                userResponse.providerType,
                authorities = mutableListOf(SimpleGrantedAuthority(userResponse.role.code)),
                level = currentLevel,
                currentExp = currentExp,
                requireExp = requireExp,
                gold = userResponse.gold,
                coreTimeHour = userResponse.coreTime.hour,
                coreTimeLastModifiedDate = userResponse.coreTimeLastModifiedDate,
                questRegistrationCount = userResponse.questRegistrationCount,
                questCompletionCount = userResponse.questCompletionCount,
                currentQuestContinuousRegistrationDays = userResponse.currentQuestContinuousRegistrationDays,
                currentQuestContinuousCompletionDays = userResponse.currentQuestContinuousCompletionDays,
                maxQuestContinuousRegistrationDays = userResponse.maxQuestContinuousRegistrationDays,
                maxQuestContinuousCompletionDays = userResponse.maxQuestContinuousCompletionDays,
                lastQuestRegistrationDate = userResponse.lastQuestRegistrationDate,
                lastQuestCompletionDate = userResponse.lastQuestCompletionDate,
                perfectDayCount = userResponse.perfectDayCount,
                goldEarnAmount = userResponse.goldEarnAmount,
                goldUseAmount = userResponse.goldUseAmount,
            )
        }
    }
}