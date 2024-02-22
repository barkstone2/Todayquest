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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DetailInteractRequest

        if (count != other.count) return false
        if (questId != other.questId) return false
        if (detailQuestId != other.detailQuestId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = count ?: 0
        result = 31 * result + questId.hashCode()
        result = 31 * result + detailQuestId.hashCode()
        return result
    }
}
