package dailyquest.preferencequest.dto

import dailyquest.preferencequest.entity.PreferenceDetailQuest
import dailyquest.preferencequest.entity.PreferenceQuest
import dailyquest.quest.entity.DetailQuestType
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.hibernate.validator.constraints.Range

data class PreferenceDetailRequest(
    @field:NotBlank(message = "{NotBlank.details.title}")
    @field:Size(max = 50, message = "{Size.details.title}")
    val title: String = "",
    val type: DetailQuestType = DetailQuestType.CHECK,
    @field:Range(min = 1, max = 255, message = "{Range.details.targetCount}")
    val targetCount: Int = 1,
    val id: Long = 0,
) {

    fun mapToEntity(preferenceQuest: PreferenceQuest): PreferenceDetailQuest {
        return PreferenceDetailQuest(
            title = title,
            type = type,
            targetCount = if (type == DetailQuestType.CHECK) 1 else targetCount,
            preferenceQuest = preferenceQuest,
        )
    }
}