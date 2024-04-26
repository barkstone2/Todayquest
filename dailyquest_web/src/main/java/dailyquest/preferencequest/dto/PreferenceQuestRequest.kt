package dailyquest.preferencequest.dto

import dailyquest.preferencequest.entity.PreferenceQuest
import dailyquest.user.entity.User
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class PreferenceQuestRequest(
    @field:NotBlank(message = "{NotBlank.quest.title}")
    @field:Size(max = 50, message = "{size.quest.title}")
    val title: String,
    @field:Size(max = 300, message = "{Size.quest.description}")
    val description: String = "",
    @field:Valid
    @field:Size(max = 5, message = "{Size.quest.details}")
    val details: List<PreferenceDetailRequest> = listOf(),
) {

    companion object {
        @JvmStatic
        fun of(title: String, description: String, details: List<PreferenceDetailRequest>): PreferenceQuestRequest {
            return PreferenceQuestRequest(title, description, details)
        }
    }

    fun mapToEntity(user: User): PreferenceQuest {
        return PreferenceQuest.of(this.title, this.description, this.details.map { it.mapToEntity() }, user)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PreferenceQuestRequest

        if (title != other.title) return false
        if (description != other.description) return false
        if (details != other.details) return false

        return true
    }

    override fun hashCode(): Int {
        var result = title.hashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + details.hashCode()
        return result
    }
}