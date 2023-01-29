package todayquest.item.entity;

import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import todayquest.common.BaseTimeEntity;
import todayquest.common.MessageUtil;
import todayquest.reward.entity.Reward;
import todayquest.user.entity.UserInfo;

import jakarta.persistence.*;

import static jakarta.persistence.FetchType.LAZY;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor @Builder
@DynamicInsert
@Entity
public class Item extends BaseTimeEntity {
    
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "user_id")
    private UserInfo user;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "reward_id")
    private Reward reward;

    private Integer count;

    public void addCount() {
        this.count++;
    }

    public void subtractCount(int count) {
        if(this.count - count < 0) throw new IllegalArgumentException(MessageUtil.getMessage("item.error.count"));
        this.count -= count;
    }

}
