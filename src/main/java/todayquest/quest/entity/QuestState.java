package todayquest.quest.entity;

import lombok.Getter;

@Getter
public enum QuestState {
    PROCEED("진행"),
    COMPLETE("완료"),
    DISCARD("포기"),
    FAIL("실패"),
    DELETE("삭제");

    private String message;

    QuestState(String message) {
        this.message = message;
    }
}
