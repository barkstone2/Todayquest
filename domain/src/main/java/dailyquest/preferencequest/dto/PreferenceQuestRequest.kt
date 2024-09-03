package dailyquest.preferencequest.dto

import dailyquest.preferencequest.entity.PreferenceQuest

interface PreferenceQuestRequest {
    val title: String
    val description: String
    val details: List<PreferenceDetailRequest>
    fun mapToEntity(userId: Long): PreferenceQuest {
        return PreferenceQuest.of(this.title, this.description, this.details.map { it.mapToEntity() }, userId)
    }
}