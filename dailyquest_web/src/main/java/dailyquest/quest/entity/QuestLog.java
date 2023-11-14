package dailyquest.quest.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import dailyquest.common.BaseLogEntity;

import jakarta.persistence.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity(name = "quest_log")
public class QuestLog extends BaseLogEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "quest_log_id")
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long questId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private QuestState state;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuestType type;

    @Builder
    public QuestLog(Long userId, Long questId, QuestState state, QuestType type) {
        this.userId = userId;
        this.questId = questId;
        this.state = state;
        this.type = type;
    }

    public QuestLog(Quest quest) {
        this.userId = quest.getUser().getId();
        this.questId = quest.getId();
        this.state = quest.getState();
        this.type = quest.getType();
    }
}
