package dailyquest.quest.dto;

import dailyquest.quest.entity.QuestState;
import dailyquest.quest.entity.QuestType;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class QuestStatisticsResponse {

    private final LocalDate loggedDate;

    private long registeredCount = 0;
    private long completeCount = 0;
    private long failCount = 0;
    private long discardCount = 0;

    private long mainCount = 0;
    private long subCount = 0;

    private long stateRatio = 0;
    private long typeRatio = 0;

    public QuestStatisticsResponse(LocalDate loggedDate) {
        this.loggedDate = loggedDate;
    }

    public void addStateCount(String state, long count) {
        switch (QuestState.valueOf(state)) {
            case PROCEED -> registeredCount += count;
            case COMPLETE -> completeCount += count;
            case DISCARD -> discardCount += count;
            case FAIL -> failCount += count;
        }
    }

    public void addTypeCount(String type, long count) {
        switch (QuestType.valueOf(type)) {
            case MAIN -> mainCount += count;
            case SUB -> subCount += count;
        }
    }

    public void calcTypeRatio() {
        long allQuestCount = mainCount + subCount;

        if(allQuestCount == 0) return;

        double ratio = mainCount * 100d / allQuestCount;
        typeRatio = Math.round(ratio);
    }

    public void calcStateRatio() {
        if(registeredCount == 0) return;

        double ratio = completeCount * 100d / registeredCount;
        stateRatio = Math.round(ratio);
    }

    public void combineCount(QuestStatisticsResponse other) {
        this.registeredCount += other.registeredCount;
        this.completeCount += other.completeCount;
        this.failCount += other.failCount;
        this.discardCount += other.discardCount;

        this.mainCount += other.mainCount;
        this.subCount += other.subCount;
    }

}
