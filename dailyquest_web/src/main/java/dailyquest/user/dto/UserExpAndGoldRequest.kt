package dailyquest.user.dto

import dailyquest.quest.entity.QuestType

data class UserExpAndGoldRequest(
    private val type: QuestType,
    private val earnedExp: Long,
    private val earnedGold: Long,
) {
    fun calculateEarnedExp(): Long {
        return earnedExp * getMultiplierBasedOnType()
    }

    fun calculateEarnedGold(): Long {
        return earnedGold * getMultiplierBasedOnType()
    }

    private fun getMultiplierBasedOnType(): Long {
        return when (type) {
            QuestType.MAIN -> 2
            QuestType.SUB -> 1
        }
    }

    companion object {
        @JvmStatic
        fun of(questType: QuestType = QuestType.SUB, earnedExp: Long = 0, earnedGold: Long = 0): UserExpAndGoldRequest {
            return UserExpAndGoldRequest(questType, earnedExp, earnedGold)
        }
    }
}