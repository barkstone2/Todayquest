package dailyquest.admin.dto

class SystemSettingsRequest(
    val questClearExp: Long,
    val questClearGold: Long,
    val maxRewardCount: Long,
)