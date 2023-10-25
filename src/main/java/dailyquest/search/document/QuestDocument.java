package dailyquest.search.document;

import dailyquest.quest.dto.DetailResponse;
import dailyquest.quest.dto.QuestResponse;
import jakarta.persistence.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    public static QuestDocument mapToDocument(QuestResponse questResponse, Long userId) {
        List<DetailResponse> detailQuests = questResponse.getDetailQuests();
        if(detailQuests == null) detailQuests = new ArrayList<>();

        List<String> detailTitles = detailQuests.stream().map(DetailResponse::getTitle).toList();
        return new QuestDocument(questResponse.getId(), questResponse.getTitle(), questResponse.getDescription(), detailTitles, userId, questResponse.getState().name(), questResponse.getCreatedDate());
    }

}
