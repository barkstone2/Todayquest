package todayquest.quest.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@Getter
public enum QuestState {
    PROCEED("진행", "bg-primary text-white rounded"),
    COMPLETE("완료", "bg-success text-white rounded"),
    DISCARD("포기", "bg-secondary text-white rounded"),
    FAIL("실패", "bg-danger text-white rounded"),
    DELETE("삭제", "bg-dark text-white rounded");

    private String message;
    private String cssClassInfo;

    QuestState(String message, String cssClassInfo) {
        this.message = message;
        this.cssClassInfo = cssClassInfo;
    }

    public static List<QuestState> getEnumListForUser() {
        return Arrays.stream(QuestState.values())
                .limit(4)
                .collect(Collectors.toList());
    }
}
