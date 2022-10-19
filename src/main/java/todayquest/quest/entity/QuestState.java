package todayquest.quest.entity;

import lombok.Getter;

@Getter
public enum QuestState {
    PROCEED("진행", "bg-primary text-white rounded"),
    COMPLETE("완료", "bg-success text-white rounded"),
    FAIL("실패", "bg-danger text-white rounded"),
    DISCARD("포기", "bg-secondary text-white rounded"),
    DELETE("삭제", "bg-dark text-white rounded");

    private String message;
    private String cssClassInfo;

    QuestState(String message, String cssClassInfo) {
        this.message = message;
        this.cssClassInfo = cssClassInfo;
    }
}
