package dailyquest.preferencequest.dto

import com.fasterxml.jackson.annotation.JsonFormat
import dailyquest.preferencequest.entity.PreferenceQuest
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
    val usedCount: Long = 0
) {

    // JPQL 생성자 호출에 사용되므로 팩토리 메서드로 전환 불가
    constructor(preferenceQuest: PreferenceQuest, usedCount: Long) : this(
        preferenceQuest.id,
        preferenceQuest.title,
        preferenceQuest.description,
        preferenceQuest.createdDate,
        preferenceQuest.lastModifiedDate,
        preferenceQuest.preferenceDetailQuests.map { PreferenceDetailResponse.from(it) },
        usedCount
    )

    companion object {
        @JvmStatic
        fun from(preferenceQuest: PreferenceQuest): PreferenceQuestResponse {
            return PreferenceQuestResponse(
                id = preferenceQuest.id,
                title = preferenceQuest.title,
                description = preferenceQuest.description,
                createdDate = preferenceQuest.createdDate,
                lastModifiedDate = preferenceQuest.lastModifiedDate,
                preferenceDetailQuests = preferenceQuest.preferenceDetailQuests.map {
                    PreferenceDetailResponse.from(it)
                },
            )
        }
    }
}