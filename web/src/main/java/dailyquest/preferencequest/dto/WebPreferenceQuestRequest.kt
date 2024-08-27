package dailyquest.preferencequest.dto

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class WebPreferenceQuestRequest(
    @field:NotBlank(message = "{NotBlank.quest.title}")
    @field:Size(max = 50, message = "{size.quest.title}")
    override val title: String,
    @field:Size(max = 300, message = "{Size.quest.description}")
    override val description: String = "",
    @field:Valid
    @field:Size(max = 5, message = "{Size.quest.details}")
    override val details: List<WebPreferenceDetailRequest> = listOf(),
) : PreferenceQuestRequest {

    companion object {
        @JvmStatic
        fun of(title: String, description: String, details: List<WebPreferenceDetailRequest>): PreferenceQuestRequest {
            return WebPreferenceQuestRequest(title, description, details)
        }
    }
}