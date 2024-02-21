package dailyquest.quest.dto

import org.hibernate.validator.constraints.Range

data class DetailInteractRequest(
    @Range(min = 0, max = 255, message = "{Range.details.count}")
    val count: Int? = null
){
    var questId: Long = 0
        private set
    var detailQuestId: Long = 0
        private set

    fun setPathVariables(questId: Long, detailQuestId: Long) {
        this.questId = questId
        this.detailQuestId = detailQuestId
    }
}
