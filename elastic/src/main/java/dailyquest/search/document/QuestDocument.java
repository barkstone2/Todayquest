package dailyquest.search.document;

import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setting(settingPath = "elastic/quests/setting.json")
@Document(indexName = "quests")
public class QuestDocument {
    @Id
    private final Long id;

    @Field(type = FieldType.Text)
    private final String title;
    @Field(type = FieldType.Text)
    private final String description;
    @Field(type = FieldType.Text)
    private final List<String> detailTitles;
    @Field(type = FieldType.Long)
    private final Long userId;
    @Field(type = FieldType.Keyword)
    private final String state;
    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    private final LocalDateTime createdDate;

    public QuestDocument(Long id, String title, String description, List<String> detailTitles, Long userId, String state, LocalDateTime createdDate) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.detailTitles = detailTitles;
        this.userId = userId;
        this.state = state;
        this.createdDate = createdDate;
    }

}
