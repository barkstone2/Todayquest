package dailyquest.preferencequest.dto

import com.fasterxml.jackson.annotation.JsonFormat
import dailyquest.preferencequest.entity.PreferenceQuest
import dailyquest.quest.dto.QuestRequest
import java.time.LocalDateTime

data class PreferenceQuestResponse(
    val id: Long = 0,
    val title: String,
    val description: String = "",
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val createdDate: LocalDateTime? = null,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val lastModifiedDate: LocalDateTime? = null,
    val preferenceDetailQuests: List<PreferenceDetailResponse> = listOf(),
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val deadLine: LocalDateTime? = null,
) {

    companion object {
        @JvmStatic
        fun createDto(preferenceQuest: PreferenceQuest): PreferenceQuestResponse {
            return PreferenceQuestResponse(
                id = preferenceQuest.id,
                title = preferenceQuest.title,
                description = preferenceQuest.description ?: "",
                createdDate = preferenceQuest.createdDate,
                lastModifiedDate = preferenceQuest.lastModifiedDate,
                preferenceDetailQuests = preferenceQuest.preferenceDetailQuests.map {
                    PreferenceDetailResponse.createDto(it)
                },
                deadLine = preferenceQuest.deadLine
            )
        }
    }

    fun mapToQuestRequest(): QuestRequest {
        return QuestRequest(
            title = title,
            description = description,
            deadLine = deadLine,
            details = preferenceDetailQuests.map { it.mapToDetailRequest() }
        )
    }
}