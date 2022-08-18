package todayquest.quest.entity;

import lombok.Getter;

@Getter
public enum QuestState {
    COMPLETE("완료"), FAIL("실패"), CANCEL("취소"), PROCEED("진행"), DELETE("삭제");
    private String stateMessage;

    QuestState(String stateMessage) {
        this.stateMessage = stateMessage;
    }
}
