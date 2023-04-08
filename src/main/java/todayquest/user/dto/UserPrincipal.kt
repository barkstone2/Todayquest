package todayquest.user.dto

import com.fasterxml.jackson.annotation.JsonGetter
import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import todayquest.user.entity.ProviderType
import todayquest.user.entity.UserInfo

class UserPrincipal(
    val userId: Long,
    var nickname: String,
    val providerType: ProviderType,
    private var authorities: MutableCollection<GrantedAuthority>,
    var level: Int,
    var currentExp: Long,
    var requireExp: Long,
    var gold: Long,
    var resetTime: Int,
    var coreTime: Int,
) : UserDetails {
    var accessToken: String? = null
    var refreshToken: String? = null

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
        fun create(userInfo: UserInfo, expTable: Map<String, Long>): UserPrincipal {

            val (currentLevel, currentExp, requireExp) = userInfo.calculateLevel(expTable)

            return UserPrincipal(
                userId = userInfo.id,
                nickname = userInfo.nickname,
                providerType = userInfo.providerType,
                authorities = mutableListOf(SimpleGrantedAuthority(userInfo.role.code)),
                level = currentLevel,
                currentExp = currentExp,
                requireExp = requireExp,
                gold = userInfo.gold,
                resetTime = userInfo.getResetHour(),
                coreTime = userInfo.getCoreHour(),
            )
        }
    }
}