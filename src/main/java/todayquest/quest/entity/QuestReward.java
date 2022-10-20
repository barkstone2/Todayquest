package todayquest.quest.entity;

import lombok.*;
import todayquest.reward.entity.Reward;

import javax.persistence.*;

import static javax.persistence.FetchType.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor @Builder
@Entity
public class QuestReward {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "quest_reward_id")
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "quest_id")
    private Quest quest;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "reward_id")
    private Reward reward;

    public void updateReward(Reward reward) {
        this.reward = reward;
    }

}
