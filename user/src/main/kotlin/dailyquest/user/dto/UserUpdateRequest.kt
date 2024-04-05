package dailyquest.user.dto

open class UserUpdateRequest(
    open val nickname: String? = null,
    val coreTime: Int? = null,
    open val earnedExp: Long = 0,
    open val earnedGold: Long = 0,
) {
}
