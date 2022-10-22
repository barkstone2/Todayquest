package todayquest.quest.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import todayquest.common.BaseLogEntity;

import javax.persistence.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class QuestLog extends BaseLogEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "quest_log_id")
    private Long id;

    private Long userId;
    private Long questId;
    @Enumerated(EnumType.STRING)
    private QuestState state;

    @Builder
    public QuestLog(Long userId, Long questId, QuestState state) {
        this.userId = userId;
        this.questId = questId;
        this.state = state;
    }
}
