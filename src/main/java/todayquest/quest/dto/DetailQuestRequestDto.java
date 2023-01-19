package todayquest.quest.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import todayquest.quest.entity.DetailQuest;
import todayquest.quest.entity.DetailQuestState;
import todayquest.quest.entity.DetailQuestType;
import todayquest.quest.entity.Quest;

@Data
@NoArgsConstructor
public class DetailQuestRequestDto {

    private String title;
    private Short count;
    private DetailQuestType type;
    private DetailQuestState state;

    public DetailQuest mapToEntity(Quest quest) {
        return DetailQuest.builder()
                .title(title)
                .type(type)
                .state(DetailQuestState.PROCEED)
                .count(type.equals(DetailQuestType.CHECK) ? 1 : count)
                .quest(quest)
                .build();
    }
}
