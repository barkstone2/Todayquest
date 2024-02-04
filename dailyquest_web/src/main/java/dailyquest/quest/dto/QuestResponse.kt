package dailyquest.quest.dto

import com.fasterxml.jackson.annotation.JsonFormat
import dailyquest.quest.entity.Quest
import dailyquest.quest.entity.QuestState
import dailyquest.quest.entity.QuestType
import dailyquest.search.document.QuestDocument
import java.time.LocalDateTime

data class QuestResponse(
    val id: Long = 0,
    val title: String = "",
    val description: String = "",
    val seq: Long = 0,
    val state: QuestState = QuestState.PROCEED,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val createdDate: LocalDateTime = LocalDateTime.now(),
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val lastModifiedDate: LocalDateTime = LocalDateTime.now(),
    val detailQuests: List<DetailResponse> = emptyList(),
    val canComplete : Boolean = false,
    val type: QuestType = QuestType.MAIN,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val deadLine: LocalDateTime? = null,
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
                    DetailResponse.createDto(it)
                },
                canComplete = quest.canComplete(),
                type = quest.type,
                deadLine = quest.deadLine
            )
        }
    }

    fun mapToDocument(userId: Long): QuestDocument {
        return QuestDocument(
            id,
            title,
            description,
            detailQuests.map { it.title },
            userId,
            state.name,
            createdDate
        )
    }

}