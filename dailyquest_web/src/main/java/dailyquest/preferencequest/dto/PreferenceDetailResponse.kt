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
        fun createDto(pdq: PreferenceDetailQuest): PreferenceDetailResponse {
            return PreferenceDetailResponse(
                id = pdq.id,
                title = pdq.title,
                targetCount = pdq.targetCount,
                type = pdq.type,
            )
        }
    }
}