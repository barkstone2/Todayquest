package dailyquest.quest.dto

import dailyquest.preferencequest.entity.PreferenceDetailQuest
import dailyquest.quest.entity.DetailQuestType

data class SimpleDetailQuestRequest(
    override val title: String,
    override val type: DetailQuestType,
    override val targetCount: Int,
    override val count: Int = 0
) : DetailQuestRequest {

    companion object {
        @JvmStatic
        fun from(preferenceDetailQuest: PreferenceDetailQuest): DetailQuestRequest {
            return SimpleDetailQuestRequest(preferenceDetailQuest.title, preferenceDetailQuest.type, preferenceDetailQuest.targetCount)
        }
    }
}