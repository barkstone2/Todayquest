package dailyquest.quest.dto

import dailyquest.quest.entity.QuestType
import dailyquest.user.dto.UserUpdateRequest

class QuestCompletionRequest @JvmOverloads constructor(
    earnedExp: Long = 0,
    earnedGold: Long = 0,
    val questId: Long,
    private var type: QuestType = QuestType.SUB,
): UserUpdateRequest(earnedExp = earnedExp, earnedGold = earnedGold) {
    override val earnedExp: Long = earnedExp
        get() = field * getMultiplierDependOnType()
    override val earnedGold: Long = earnedGold
        get() = field * getMultiplierDependOnType()

    private fun getMultiplierDependOnType(): Long {
        return when (type) {
            QuestType.MAIN -> 2L
            QuestType.SUB -> 1L
        }
    }

    fun toMainQuest() {
        this.type = QuestType.MAIN
    }
}