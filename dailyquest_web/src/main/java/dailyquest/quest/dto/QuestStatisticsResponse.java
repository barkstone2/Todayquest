package dailyquest.quest.dto;

import dailyquest.quest.entity.QuestState;
import dailyquest.quest.entity.QuestType;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class QuestStatisticsResponse {

    private final LocalDate loggedDate;

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

        double ratio = (double) mainCount / allQuestCount;
        double percent = ratio * 100;
        typeRatio = Math.round(percent);
    }

    public void calcStateRatio() {
        long allQuestCount = completeCount + failCount + discardCount;

        if(allQuestCount == 0) return;

        double ratio = (double) completeCount / allQuestCount;
        double percent = ratio * 100;
        stateRatio = Math.round(percent);
    }

    public void combineCount(QuestStatisticsResponse other) {
        this.completeCount += other.completeCount;
        this.failCount += other.failCount;
        this.discardCount += other.discardCount;

        this.mainCount += other.mainCount;
        this.subCount += other.subCount;
    }

}
