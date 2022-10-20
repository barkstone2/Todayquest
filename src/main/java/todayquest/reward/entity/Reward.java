package todayquest.reward.entity;

import lombok.*;
import todayquest.reward.dto.RewardRequestDto;
import todayquest.user.entity.UserInfo;

import javax.persistence.*;

import static javax.persistence.FetchType.LAZY;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor @Builder
@Entity
public class Reward {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reward_id")
    private Long id;

    @Column(length = 30, nullable = false)
    private String name;

    @Column(length = 100)
    private String description;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "user_id")
    private UserInfo user;

    @Enumerated(EnumType.STRING)
    private RewardGrade grade;

    public void updateReward(RewardRequestDto dto) {
        this.name = dto.getName();
        this.description = dto.getDescription();
        this.grade = dto.getGrade();
    }
}
