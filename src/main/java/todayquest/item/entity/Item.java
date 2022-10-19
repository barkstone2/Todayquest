package todayquest.item.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import todayquest.common.MessageUtil;
import todayquest.reward.entity.Reward;
import todayquest.user.entity.UserInfo;

import javax.persistence.*;

import static javax.persistence.FetchType.LAZY;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicInsert
@Entity
public class Item {
    
    @Id @GeneratedValue
    @Column(name = "item_id")
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "user_id")
    private UserInfo user;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "reward_id")
    private Reward reward;

    private int count;

    @Builder
    public Item(UserInfo user, Reward reward) {
        this.user = user;
        this.reward = reward;
    }

    public void addCount() {
        this.count++;
    }

    public void subtractCount(int count) {
        if(this.count - count < 0) throw new IllegalArgumentException(MessageUtil.getMessage("item.error.count"));
        this.count -= count;
    }

}
