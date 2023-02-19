package todayquest.quest.dto;

import jakarta.validation.constraints.Min;
import lombok.Data;
import todayquest.quest.entity.QuestState;

@Data
public class QuestSearchCondition {

    @Min(0)
    private int page;
    private QuestState state;

    public QuestSearchCondition(){
        this.state = QuestState.PROCEED;
        this.page = 0;
    }

    public QuestSearchCondition(int page, QuestState state) {
        this.page = page;
        this.state = state;
    }
}
