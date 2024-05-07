package dailyquest.user.dto

import dailyquest.user.entity.User
import java.io.Serializable

class UserPerfectDayCount(
    val userId: Long,
    val perfectDayCount: Long
) : Serializable {
    companion object {
        @JvmStatic
        fun from(user: User): UserPerfectDayCount {
            return UserPerfectDayCount(user.id, user.perfectDayCount)
        }
    }
}