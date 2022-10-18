package todayquest.quest.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import todayquest.quest.entity.Quest;
import todayquest.quest.entity.QuestDifficulty;
import todayquest.quest.entity.QuestState;
import todayquest.quest.entity.QuestType;
import todayquest.reward.dto.RewardResponseDto;
import todayquest.user.entity.UserInfo;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;


@Data
@NoArgsConstructor
public class QuestRequestDto {

    @NotBlank
    private String title;
    @NotBlank
    private String description;
    @NotNull
    private boolean isRepeat;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate deadLineDate;
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime deadLineTime;
    @NotNull
    private QuestDifficulty difficulty;
    @Size(max = 5)
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

        this.title = title;
        this.description = description;
        this.isRepeat = isRepeat;
        this.deadLineDate = deadLineDate;
        this.deadLineTime = deadLineTime;
        this.difficulty = difficulty;
        this.rewards = rewards;
    }

    public QuestResponseDto mapToResponseDto(List<RewardResponseDto> rewards) {
        return QuestResponseDto.builder()
                .title(getTitle())
                .description(getDescription())
                .difficulty(getDifficulty())
                .deadLineTime(getDeadLineTime())
                .deadLineDate(getDeadLineDate())
                .isRepeat(isRepeat())
                .rewards(rewards)
                .build();
    }
}
