package dailyquest.quest.dto

data class DetailInteractRequest(
    val questId: Long,
    val detailQuestId: Long,
    val count: Int? = null
){

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
