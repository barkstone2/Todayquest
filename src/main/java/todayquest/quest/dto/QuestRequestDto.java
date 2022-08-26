package todayquest.quest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import todayquest.quest.entity.*;
import todayquest.user.entity.UserInfo;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;


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
    private QuestDifficulty difficulty;
    private List<String> rewards;

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
                .difficulty(getDifficulty())
                .rewards(getRewardEntityList())
                .build();
    }

    public List<QuestReward> getRewardEntityList() {
        return getRewards().stream().map(s -> QuestReward.builder().reward(s).build()).collect(Collectors.toList());
    }



}
