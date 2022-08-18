package todayquest.quest.entity;

import lombok.Getter;

@Getter
public enum QuestType {
    DAILY("일간"), WEEKLY("주간"), MONTHLY("월간"), ANNUALLY("연간");

    private String typeMessage;

    QuestType(String typeMessage) {
        this.typeMessage = typeMessage;
    }
}
