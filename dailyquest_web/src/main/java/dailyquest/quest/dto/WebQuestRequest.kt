package dailyquest.quest.dto

import dailyquest.preferencequest.entity.PreferenceQuest
import dailyquest.quest.entity.QuestType
import dailyquest.validation.constratins.DeadLineRange
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

data class WebQuestRequest(
    @field:NotBlank(message = "{NotBlank.quest.title}")
    @field:Size(max = 50, message = "{size.quest.title}")
    override val title: String,
    @field:Size(max = 300, message = "{Size.quest.description}")
    override val description: String = "",
    @field:Valid
    @field:Size(max = 5, message = "{Size.quest.details}")
    override val details: List<WebDetailQuestRequest> = emptyList(),
    @field:DeadLineRange("{Range.quest.deadLine}")
    override val deadLine: LocalDateTime? = null,
    override val preferenceQuest: PreferenceQuest? = null,
): QuestRequest {
    override var type: QuestType = QuestType.SUB
        private set

    fun toMainQuest() {
        this.type = QuestType.MAIN
    }

    companion object {
        @JvmStatic
        fun from(preferenceQuest: PreferenceQuest): WebQuestRequest {
            return WebQuestRequest(
                title = preferenceQuest.title,
                description = preferenceQuest.description,
                details = preferenceQuest.preferenceDetailQuests.map { WebDetailQuestRequest(it) },
                preferenceQuest = preferenceQuest
            )
        }
    }
}
