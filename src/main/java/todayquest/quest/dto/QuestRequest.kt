package todayquest.quest.dto

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import todayquest.quest.entity.Quest
import todayquest.quest.entity.QuestState
import todayquest.quest.entity.QuestType
import todayquest.user.entity.UserInfo

class QuestRequest(
    title: String,
    description: String,
    details: MutableList<DetailRequest>? = null
) {

    @NotBlank(message = "{NotBlank.quest.title}")
    @Size(max = 50, message = "{size.quest.title}")
    val title = title

    @NotBlank(message = "{NotBlank.quest.description}")
    @Size(max = 300, message = "{Size.quest.description}")
    val description = description

    @Valid
    @Size(max = 5, message = "{Size.quest.details}")
    val details = details ?: mutableListOf()

    private var type: QuestType = QuestType.SUB

    fun toMainQuest() {
        this.type = QuestType.MAIN
    }

    fun mapToEntity(nextSeq: Long, userInfo: UserInfo): Quest {
        return Quest(
            title = title,
            description = description,
            user = userInfo,
            seq = nextSeq,
            state = QuestState.PROCEED,
            type = type
        )
    }
}
