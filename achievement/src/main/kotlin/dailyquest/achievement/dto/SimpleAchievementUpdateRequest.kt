package dailyquest.achievement.dto

data class SimpleAchievementUpdateRequest(
    override val title: String,
    override val description: String
) : AchievementUpdateRequest