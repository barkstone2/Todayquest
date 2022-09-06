package todayquest.quest.dto;

import com.mysema.commons.lang.Assert;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.util.StringUtils;
import todayquest.quest.entity.*;
import todayquest.user.entity.UserInfo;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Data
@NoArgsConstructor
public class QuestRequestDto {

    private String title;
    private String description;
    private boolean isRepeat;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate deadLineDate;
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime deadLineTime;
    private QuestDifficulty difficulty;
    private List<String> rewards = new ArrayList<>();

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

    @Builder
    public QuestRequestDto(String title, String description, boolean isRepeat, LocalDate deadLineDate, LocalTime deadLineTime, QuestDifficulty difficulty, List<String> rewards) {
        Assert.hasText(title, "title must not be empty");
        Assert.notNull(isRepeat, "isRepeat must not be null");
        Assert.notNull(deadLineDate, "deadLineDate must not be null");
        Assert.notNull(difficulty, "difficulty must not be null");

        this.title = title;
        this.description = description;
        this.isRepeat = isRepeat;
        this.deadLineDate = deadLineDate;
        this.deadLineTime = deadLineTime;
        this.difficulty = difficulty;
        this.rewards = rewards;
    }

    public List<QuestReward> getRewardEntityList() {
        return getRewards().stream().filter(StringUtils::hasText).map(s -> QuestReward.builder().reward(s).build()).collect(Collectors.toList());
    }



}
