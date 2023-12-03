package dailyquest.quest.dto;

import dailyquest.quest.entity.QuestState;
import jakarta.validation.constraints.Min;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

public record QuestSearchCondition(
    @Min(0)
    Integer page,
    QuestState state,
    QuestSearchKeywordType keywordType,
    String keyword,
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    LocalDate startDate,
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    LocalDate endDate
) {

    public QuestSearchCondition(Integer page, QuestState state, QuestSearchKeywordType keywordType, String keyword, LocalDate startDate, LocalDate endDate) {
        this.page = Objects.requireNonNullElse(page, 0);
        this.state = state;
        this.keywordType = keywordType;
        this.keyword = keyword;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public boolean isKeywordSearch() {
        return Objects.nonNull(keywordType) && Objects.nonNull(keyword) && !keyword.isBlank();
    }

    public LocalDateTime getStartResetTime() {
        return startDate == null ? null : LocalDateTime.of(startDate, LocalTime.of(6, 0));
    }

    public LocalDateTime getEndResetTime() {
        return endDate == null ? null : LocalDateTime.of(endDate.plusDays(1L), LocalTime.of(6, 0));
    }
}
