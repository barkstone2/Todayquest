package dailyquest.quest.dto

import dailyquest.preferencequest.entity.PreferenceQuest
import dailyquest.quest.entity.Quest
import dailyquest.quest.entity.QuestState
import dailyquest.quest.entity.QuestType
import dailyquest.user.entity.User
import dailyquest.validation.constratins.DeadLineRange
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

data class QuestRequest(
    @field:NotBlank(message = "{NotBlank.quest.title}")
    @field:Size(max = 50, message = "{size.quest.title}")
    val title: String,
    @field:Size(max = 300, message = "{Size.quest.description}")
    val description: String = "",
    @field:Valid
    @field:Size(max = 5, message = "{Size.quest.details}")
    val details: List<DetailRequest> = emptyList(),
    @field:DeadLineRange("{Range.quest.deadLine}")
    val deadLine: LocalDateTime? = null,
    private val preferenceQuest: PreferenceQuest? = null,
) {
    var type: QuestType = QuestType.SUB
        private set

    companion object {
        @JvmStatic
        fun from(preferenceQuest: PreferenceQuest): QuestRequest {
            return QuestRequest(
                title = preferenceQuest.title,
                description = preferenceQuest.description,
                details = preferenceQuest.preferenceDetailQuests.map { DetailRequest(it) },
                deadLine = preferenceQuest.deadLine,
                preferenceQuest = preferenceQuest
            )
        }
    }

    fun toMainQuest() {
        this.type = QuestType.MAIN
    }

    fun mapToEntity(nextSeq: Long, user: User): Quest {
        val quest = Quest(
            title = title,
            description = description,
            user = user,
            seq = nextSeq,
            state = QuestState.PROCEED,
            type = type,
            deadline = deadLine,
            preferenceQuest = preferenceQuest
        )
        quest.replaceDetailQuests(this.details.map { it.mapToEntity(quest) })
        return quest
    }
}
