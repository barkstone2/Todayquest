package todayquest.item.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import todayquest.common.BaseLogEntity;

import javax.persistence.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class ItemLog extends BaseLogEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_log_id")
    private Long id;

    private Long userId;
    private Long rewardId;

    @Enumerated(EnumType.STRING)
    private ItemLogType type;

    @Builder
    public ItemLog(Long userId, Long rewardId, ItemLogType type) {
        this.userId = userId;
        this.rewardId = rewardId;
        this.type = type;
    }
}
