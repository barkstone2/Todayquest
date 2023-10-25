package dailyquest.quest.dto;

import jakarta.validation.constraints.Min;
import dailyquest.quest.entity.QuestState;
import java.util.Objects;

public record QuestSearchCondition(
    @Min(0)
    Integer page,
    QuestState state,
    QuestSearchKeywordType keywordType,
    String keyword
) {

    public QuestSearchCondition(Integer page, QuestState state, QuestSearchKeywordType keywordType, String keyword) {
        this.page = Objects.requireNonNullElse(page, 0);
        this.state = Objects.requireNonNullElse(state, QuestState.PROCEED);
        this.keywordType = keywordType;
        this.keyword = keyword;
    }

    public boolean isKeywordSearch() {
        return Objects.nonNull(keywordType) && Objects.nonNull(keyword);
    }
}
