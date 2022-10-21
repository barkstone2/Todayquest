package todayquest.quest.dto;

import lombok.Data;
import todayquest.quest.entity.QuestState;

@Data
public class QuestSearchCondition {

    private int page;
    private QuestState state;

    public QuestSearchCondition(){
        this.state = QuestState.PROCEED;
        this.page = 0;
    }
}
