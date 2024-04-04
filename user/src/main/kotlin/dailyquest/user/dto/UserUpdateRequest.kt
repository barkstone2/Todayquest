package dailyquest.user.dto

open class UserUpdateRequest(
    val nickname: String? = null,
    val coreTime: Int? = null,
    val earnedExp: Long = 0,
    val earnedGold: Long = 0,
) {
}
