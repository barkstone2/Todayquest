package dailyquest.quest.dto

import dailyquest.common.MessageUtil
import dailyquest.quest.entity.DetailQuest
import dailyquest.quest.entity.DetailQuestState
import dailyquest.quest.entity.DetailQuestType

data class DetailResponse(
    val id: Long? = null,
    val title: String? = null,
    val targetCount: Int? = null,
    val count: Int? = null,
    val type: DetailQuestType? = null,
    val state: DetailQuestState? = null,
    var canCompleteParent: Boolean? = false,
) {

    companion object {
        @JvmStatic
        fun of(interactResult: DetailQuest?, canCompleteParent: Boolean? = false): DetailResponse {
            checkNotNull(interactResult) { MessageUtil.getMessage("exception.badRequest") }
            return DetailResponse(
                id = interactResult.id,
                title = interactResult.title,
                targetCount = interactResult.targetCount,
                count = interactResult.count,
                type = interactResult.type,
                state = interactResult.state,
                canCompleteParent = canCompleteParent
            )
        }
    }
}