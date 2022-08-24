package todayquest.quest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import todayquest.quest.entity.Quest;
import todayquest.quest.entity.QuestState;
import todayquest.quest.entity.QuestType;
import todayquest.user.entity.UserInfo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;


@Data
@NoArgsConstructor
@AllArgsConstructor @Builder
public class QuestRequestDto {

    private String title;
    private String description;
    private boolean isRepeat;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate deadLineDate;
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime deadLineTime;

    public Quest mapToEntity(UserInfo userInfo) {
        return Quest.builder()
                .title(getTitle())
                .user(userInfo)
                .description(getDescription())
                .isRepeat(isRepeat())
                .deadLineDate(getDeadLineDate())
                .deadLineTime(getDeadLineTime())
                .type(QuestType.DAILY)
                .state(QuestState.PROCEED)
                .build();
    }



}
