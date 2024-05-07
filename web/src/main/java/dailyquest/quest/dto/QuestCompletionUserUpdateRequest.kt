package dailyquest.quest.dto

import dailyquest.quest.entity.QuestType
import dailyquest.user.dto.UserUpdateRequest

class QuestCompletionUserUpdateRequest @JvmOverloads constructor(
    earnedExp: Long = 0,
    earnedGold: Long = 0,
    private var type: QuestType,
): UserUpdateRequest(earnedExp = earnedExp, earnedGold = earnedGold) {
    override val earnedExp: Long = earnedExp
        get() = field * getMultiplierDependOnType()
    override val earnedGold: Long = earnedGold
        get() = field * getMultiplierDependOnType()

    private fun getMultiplierDependOnType(): Long {
        return when (type) {
            QuestType.MAIN -> 2
            QuestType.SUB -> 1
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as QuestCompletionUserUpdateRequest

        if (type != other.type) return false
        if (earnedExp != other.earnedExp) return false
        if (earnedGold != other.earnedGold) return false

        return true
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + earnedExp.hashCode()
        result = 31 * result + earnedGold.hashCode()
        return result
    }
}