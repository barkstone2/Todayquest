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
    var exp: Long,
    var gold: Long,
    var resetTime: Int,
    var coreTime: Int,
) : UserDetails {
    var accessToken: String? = null
    var refreshToken: String? = null


    fun synchronizeUserInfo(user: UserInfo) {
        level = user.level
        exp = user.exp
        gold = user.gold
    }

    fun changeUserSettings(dto: UserRequestDto) {
        resetTime = dto.resetTime
        coreTime = dto.coreTime
    }

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
        fun create(userInfo: UserInfo): UserPrincipal {

            return UserPrincipal(
                userInfo.id,
                userInfo.nickname,
                userInfo.providerType,
                mutableListOf(SimpleGrantedAuthority(RoleType.USER.code)),
                userInfo.level,
                userInfo.exp,
                userInfo.gold,
                userInfo.resetTime.hour,
                userInfo.coreTime.hour,
            )
        }
    }
}