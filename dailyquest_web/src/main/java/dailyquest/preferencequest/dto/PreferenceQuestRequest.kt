package dailyquest.preferencequest.dto

import dailyquest.common.MessageUtil
import dailyquest.preferencequest.entity.PreferenceQuest
import dailyquest.user.entity.UserInfo
import dailyquest.util.dto.DeadLineBoundaryResolver
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
    val boundaryResolver: DeadLineBoundaryResolver = DeadLineBoundaryResolver()
) {

    companion object {
        @JvmStatic
        fun of(title: String, description: String, details: List<PreferenceDetailRequest>, deadLine: LocalDateTime?): PreferenceQuestRequest {
            return PreferenceQuestRequest(title, description, details, deadLine)
        }
    }

    fun mapToEntity(userInfo: UserInfo): PreferenceQuest {
        if (isValidDeadLine())
            return PreferenceQuest.of(this.title, this.description, this.deadLine, this.details.map { it.mapToEntity() }, userInfo)
        throw IllegalArgumentException(MessageUtil.getMessage("Range.quest.deadLine"))
    }

    fun isValidDeadLine(): Boolean {
        if (this.deadLine == null) return true
        val deadLine = deadLineWithoutSecondAndNano()
        val minBoundary = boundaryResolver.resolveMinBoundary()
        val maxBoundary = boundaryResolver.resolveMaxBoundary()
        return deadLine.isAfter(minBoundary) && deadLine.isBefore(maxBoundary)
    }

    private fun deadLineWithoutSecondAndNano(): LocalDateTime =
        deadLine!!.withSecond(0).withNano(0)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PreferenceQuestRequest

        if (title != other.title) return false
        if (description != other.description) return false
        if (details != other.details) return false
        if (deadLine != other.deadLine) return false

        return true
    }

    override fun hashCode(): Int {
        var result = title.hashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + details.hashCode()
        result = 31 * result + (deadLine?.hashCode() ?: 0)
        return result
    }
}