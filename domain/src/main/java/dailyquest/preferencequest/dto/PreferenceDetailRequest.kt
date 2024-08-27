package dailyquest.preferencequest.dto

import dailyquest.preferencequest.entity.PreferenceDetailQuest
import dailyquest.quest.entity.DetailQuestType

interface PreferenceDetailRequest {
    val title: String
    val type: DetailQuestType
    val targetCount: Int

    fun mapToEntity(): PreferenceDetailQuest {
        return PreferenceDetailQuest.of(
            title = title,
            type = type,
            targetCount = if (type == DetailQuestType.CHECK) 1 else targetCount,
        )
    }
}