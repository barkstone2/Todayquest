package dailyquest.preferencequest.dto

import dailyquest.preferencequest.entity.PreferenceQuest
import java.time.LocalDateTime

data class PreferenceQuestResponse(
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val createdDate: LocalDateTime? = null,
    val lastModifiedDate: LocalDateTime? = null,
    val preferenceDetailQuests: List<PreferenceDetailResponse> = listOf(),
    val usedCount: Long = 0
) {
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

        @JvmStatic
        fun of(preferenceQuest: PreferenceQuest, usedCount: Long): PreferenceQuestResponse {
            return PreferenceQuestResponse(
                preferenceQuest.id,
                preferenceQuest.title,
                preferenceQuest.description,
                preferenceQuest.createdDate,
                preferenceQuest.lastModifiedDate,
                preferenceQuest.preferenceDetailQuests.map { PreferenceDetailResponse.from(it) },
                usedCount
            )
        }
    }
}