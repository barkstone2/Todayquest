package dailyquest.achievement.entity

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnore

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
enum class AchievementType(@JsonIgnore val representationFormat: String, val order: Int, val displayName: String) {
    QUEST_REGISTRATION("퀘스트 %d개 등록", 1, "퀘스트 등록 횟수"),
    QUEST_COMPLETION("퀘스트 %d개 완료", 2, "퀘스트 완료 횟수"),
    QUEST_CONTINUOUS_REGISTRATION("퀘스트 %d일 연속 등록", 3, "퀘스트 연속 등록일"),
    QUEST_CONTINUOUS_COMPLETION("퀘스트 %d일 연속 완료", 4, "퀘스트 연속 완료일"),
    GOLD_EARN("총 골드 %,d 획득", 5, "총 골드 획득"),
    PERFECT_DAY("완벽한 하루 %d회 달성", 6, "완벽한 하루 횟수");

    fun getKey(): String {
        return name
    }

    override fun toString(): String {
        return this.name
    }
}