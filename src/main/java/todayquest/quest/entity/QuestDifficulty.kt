package todayquest.quest.entity

import com.fasterxml.jackson.annotation.JsonFormat
import java.util.*
import java.util.stream.Collectors

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
enum class QuestDifficulty(
    var experience: Int,
    var gold: Int,
    var text: String
) {
    VERY_EASY(1, 10, "매우 쉬움"),
    EASY(2, 20, "쉬움"),
    NORMAL(3, 30, "보통"),
    HARD(4, 40, "어려움"),
    VERY_HARD(5, 50, "매우 어려움");

    companion object {
        @JvmStatic
        val enumList: List<QuestDifficulty>
            get() = Arrays.stream(values())
                .collect(Collectors.toList())
    }
}