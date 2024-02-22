package dailyquest.quest.dto

import com.fasterxml.jackson.annotation.JsonFormat
import dailyquest.quest.entity.Quest
import dailyquest.quest.entity.QuestState
import dailyquest.quest.entity.QuestType
import dailyquest.search.document.QuestDocument
import java.time.LocalDateTime

data class QuestResponse(
    var id: Long? = null,
    var title: String? = null,
    var description: String? = null,
    var seq: Long? = null,
    var state: QuestState? = null,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    var createdDate: LocalDateTime? = null,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    var lastModifiedDate: LocalDateTime? = null,
    var detailQuests: List<DetailResponse>? = null,
    var canComplete : Boolean = false,
    var type: QuestType? = null,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    var deadLine: LocalDateTime? = null,
) {

    companion object {
        @JvmStatic
        fun createDto(quest: Quest): QuestResponse {
            return QuestResponse(
                id = quest.id,
                title = quest.title,
                description = quest.description,
                seq = quest.seq,
                state = quest.state,
                createdDate = quest.createdDate,
                lastModifiedDate = quest.lastModifiedDate,
                detailQuests = quest.detailQuests.map {
                    DetailResponse.of(it)
                }.toCollection(mutableListOf()),
                canComplete = quest.canComplete(),
                type = quest.type,
                deadLine = quest.deadLine
            )
        }
    }

    fun mapToDocument(userId: Long): QuestDocument {
        var detailQuests = detailQuests
        if (detailQuests == null) detailQuests = ArrayList()

        val detailTitles = detailQuests.stream().map<String>(DetailResponse::title).toList()
        return QuestDocument(
            id,
            title,
            description,
            detailTitles,
            userId,
            state?.name,
            createdDate
        )
    }

}