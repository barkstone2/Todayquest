package dailyquest.quest.entity;

import dailyquest.common.BaseTimeEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;

import java.time.LocalDate;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name ="quest_log")
@Entity
public class QuestLog extends BaseTimeEntity {

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

    @Column(nullable = false, updatable = false)
    private LocalDate loggedDate;

    @Builder
    public QuestLog(Long userId, Long questId, QuestState state, QuestType type, LocalDate loggedDate) {
        this.userId = userId;
        this.questId = questId;
        this.state = state;
        this.type = type;
        this.loggedDate = loggedDate;
    }

    public QuestLog(Quest quest, LocalDate loggedDate) {
        this.userId = quest.getUserId();
        this.questId = quest.getId();
        this.state = quest.getState();
        this.type = quest.getType();
        this.loggedDate = loggedDate;
    }
}
