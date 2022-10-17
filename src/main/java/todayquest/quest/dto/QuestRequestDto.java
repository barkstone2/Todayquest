package todayquest.quest.dto;

import com.mysema.commons.lang.Assert;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import todayquest.quest.entity.Quest;
import todayquest.quest.entity.QuestDifficulty;
import todayquest.quest.entity.QuestState;
import todayquest.quest.entity.QuestType;
import todayquest.user.entity.UserInfo;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;


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
    private List<Long> rewards = new ArrayList<>();

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
                .build();
    }

    @Builder
    public QuestRequestDto(String title, String description, boolean isRepeat, LocalDate deadLineDate, LocalTime deadLineTime, QuestDifficulty difficulty, List<Long> rewards) {
        Assert.hasText(title, "title must not be empty");
        Assert.notNull(isRepeat, "isRepeat must not be null");
        Assert.notNull(deadLineDate, "deadLineDate must not be null");
        Assert.notNull(difficulty, "difficulty must not be null");
        if(rewards != null) Assert.isFalse(rewards.size() > 5, "rewards size must not exceed 5");

        this.title = title;
        this.description = description;
        this.isRepeat = isRepeat;
        this.deadLineDate = deadLineDate;
        this.deadLineTime = deadLineTime;
        this.difficulty = difficulty;
        this.rewards = rewards;
    }

}
