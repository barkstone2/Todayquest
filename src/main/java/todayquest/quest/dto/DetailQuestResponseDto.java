package todayquest.quest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import todayquest.quest.entity.DetailQuest;
import todayquest.quest.entity.DetailQuestState;
import todayquest.quest.entity.DetailQuestType;

@Data
@NoArgsConstructor
@AllArgsConstructor @Builder
public class DetailQuestResponseDto {
    private String title;
    private Short count;
    private DetailQuestType type;
    private DetailQuestState state;

    public static DetailQuestResponseDto createDto(DetailQuest dq) {
        return DetailQuestResponseDto.builder()
                .title(dq.getTitle())
                .count(dq.getTargetCount())
                .type(dq.getType())
                .state(dq.getState())
                .build();
    }
}
