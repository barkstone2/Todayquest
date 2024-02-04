package dailyquest.preferencequest.dto

import dailyquest.common.MessageUtil
import dailyquest.preferencequest.entity.PreferenceQuest
import dailyquest.user.entity.UserInfo
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

data class PreferenceQuestRequest(
    @field:NotBlank(message = "{NotBlank.quest.title}")
    @field:Size(max = 50, message = "{size.quest.title}")
    val title: String,
    @field:Size(max = 300, message = "{Size.quest.description}")
    val description: String = "",
    @field:Valid
    @field:Size(max = 5, message = "{Size.quest.details}")
    val details: List<PreferenceDetailRequest> = listOf(),
    val deadLine: LocalDateTime? = null,
) {

    fun mapToEntity(userInfo: UserInfo): PreferenceQuest {
        return PreferenceQuest(
            title = title,
            description = description,
            user = userInfo,
            deadLine = deadLine
        )
    }

    fun checkRangeOfDeadLine() {
        if (deadLine != null) {
            val now = LocalDateTime.now().withSecond(0).withNano(0)
            var nextReset = now.withHour(6).withMinute(0)
            if(now.isEqual(nextReset) || now.isAfter(nextReset)) nextReset = nextReset.plusDays(1L)

            require(deadLine.isAfter(now.plusMinutes(5)) && deadLine.isBefore(nextReset.minusMinutes(5))) { MessageUtil.getMessage("Range.quest.deadLine") }
        }
    }

}