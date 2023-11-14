package dailyquest.quest.dto

enum class QuestLogSearchType(var title: String) {
    DAILY("일"),
    WEEKLY("주"),
    MONTHLY("월"),
    YEARLY("연"),
}