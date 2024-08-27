package dailyquest.preferencequest.dto

import dailyquest.preferencequest.entity.PreferenceQuest
import dailyquest.user.entity.User

interface PreferenceQuestRequest {
    val title: String
    val description: String
    val details: List<PreferenceDetailRequest>
    fun mapToEntity(user: User): PreferenceQuest {
        return PreferenceQuest.of(this.title, this.description, this.details.map { it.mapToEntity() }, user)
    }
}