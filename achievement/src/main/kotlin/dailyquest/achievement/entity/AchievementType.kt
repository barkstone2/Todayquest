package dailyquest.achievement.entity

enum class AchievementType(val representationFormat: String) {
    QUEST_REGISTRATION("퀘스트 %d개 등록"),
    QUEST_COMPLETION("퀘스트 %d개 완료"),
    QUEST_CONTINUOUS_REGISTRATION_DAYS("퀘스트 %d일 연속 등록"),
    QUEST_CONTINUOUS_COMPLETION("퀘스트 %d일 연속 완료"),
    USER_LEVEL("레벨 %d 달성"),
    GOLD_EARN("총 골드 %,d 획득"),
    EMPTY("");

    override fun toString(): String {
        return this.name
    }
}