package todayquest.quest.entity;

import lombok.*;
import todayquest.reward.entity.Reward;

import javax.persistence.*;

import static javax.persistence.FetchType.*;

/**
 * 퀘스트 - 리워드 다대다 매핑 엔티티
 * 추후에 리워드 엔티티에 유저 정보가 들어가지 않는 경우가 발생할 예정이라 추가 -> 파티 리워드
 */
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
