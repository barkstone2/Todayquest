package dailyquest.preferencequest.dto

import dailyquest.preferencequest.entity.PreferenceDetailQuest
import dailyquest.quest.entity.DetailQuestType

data class PreferenceDetailResponse(
    val id: Long = 0,
    val title: String = "",
    val targetCount: Int = 1,
    val type: DetailQuestType = DetailQuestType.CHECK,
) {
    companion object {
        fun from(preferenceDetailQuest: PreferenceDetailQuest): PreferenceDetailResponse {
            return PreferenceDetailResponse(
                id = preferenceDetailQuest.id,
                title = preferenceDetailQuest.title,
                targetCount = preferenceDetailQuest.targetCount,
                type = preferenceDetailQuest.type,
            )
        }
    }
}