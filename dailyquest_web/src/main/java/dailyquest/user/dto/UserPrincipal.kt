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

class UserPrincipal(
    val id: Long,
    val nickname: String,
    val providerType: ProviderType = ProviderType.GOOGLE,
    private val authorities: MutableCollection<GrantedAuthority> = mutableListOf(SimpleGrantedAuthority(RoleType.USER.code)),
    val level: Int = 0,
    val currentExp: Long = 0,
    val requireExp: Long = 0,
    val gold: Long = 0,
    val coreTime: Int = 8,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val coreTimeLastModifiedDate: LocalDateTime? = null,
    val questRegistrationCount: Int = 0,
    val questCompletionCount: Int = 0,
    val currentQuestContinuousRegistrationDays: Int = 0,
    val currentQuestContinuousCompletionDays: Int = 0,
    val maxQuestContinuousRegistrationDays: Int = 0,
    val maxQuestContinuousCompletionDays: Int = 0,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    val lastQuestRegistrationDate: LocalDate? = null,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    val lastQuestCompletionDate: LocalDate? = null,
    val perfectDayCount: Int = 0,
    val goldEarnAmount: Long = 0,
    val goldUseAmount: Long = 0,
) : UserDetails {

    @JsonIgnore
    override fun getAuthorities(): MutableCollection<out GrantedAuthority> {
        return authorities
    }

    @JsonGetter("authorities")
    fun getAuthorityValues(): List<String> {
        return getAuthorities().map { it.authority }
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
                coreTime = userResponse.coreTime.hour,
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