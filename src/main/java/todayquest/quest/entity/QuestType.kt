package todayquest.quest.entity

import com.fasterxml.jackson.annotation.JsonFormat

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
enum class QuestType(
    var title: String
) {
    MAIN("메인"),
    SUB("서브")
}

