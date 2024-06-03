package dailyquest.user.dto

import dailyquest.user.record.entity.UserRecord
import java.io.Serializable

class UserPerfectDayCount(
    val userId: Long,
    val perfectDayCount: Long
) : Serializable {
    companion object {
        @JvmStatic
        fun from(userRecord: UserRecord): UserPerfectDayCount {
            return UserPerfectDayCount(userRecord.id, userRecord.perfectDayCount)
        }
    }
}