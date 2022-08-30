package todayquest.quest.entity;

import lombok.*;

import javax.persistence.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor @Builder
@Entity
public class QuestReward {

    @Id @GeneratedValue
    @Column(name = "reward_id")
    private Long id;

    @Column(length = 30, nullable = false)
    private String reward;

    public void updateReward(String reward) {
        this.reward = reward;
    }
}
