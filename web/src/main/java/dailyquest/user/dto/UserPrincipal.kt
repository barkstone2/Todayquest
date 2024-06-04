package dailyquest.user.dto

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
    val coreTimeLastModifiedDate: LocalDateTime? = null,
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
            )
        }
    }
}