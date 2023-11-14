package dailyquest.quest.dto;

import dailyquest.quest.entity.QuestState;
import jakarta.validation.constraints.Min;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.Objects;

public record QuestSearchCondition(
    @Min(0)
    Integer page,
    QuestState state,
    QuestSearchKeywordType keywordType,
    String keyword,
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime startDate,
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime endDate
) {

    public QuestSearchCondition(Integer page, QuestState state, QuestSearchKeywordType keywordType, String keyword, LocalDateTime startDate, LocalDateTime endDate) {
        this.page = Objects.requireNonNullElse(page, 0);
        this.state = state;
        this.keywordType = keywordType;
        this.keyword = keyword;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public boolean isKeywordSearch() {
        return Objects.nonNull(keywordType) && Objects.nonNull(keyword);
    }
}
