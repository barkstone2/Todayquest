package dailyquest.user.dto

import dailyquest.quest.entity.QuestType

data class UserExpAndGoldRequest(
    val type: QuestType,
    val earnedExp: Long = 0,
    val earnedGold: Long = 0
) {

    companion object {
        @JvmStatic
        fun of(questType: QuestType = QuestType.SUB, earnedExp: Long = 0, earnedGold: Long = 0): UserExpAndGoldRequest {
            return UserExpAndGoldRequest(questType, earnedExp, earnedGold)
        }
    }
}