package todayquest.achievement.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import todayquest.user.entity.UserInfo;

import javax.persistence.*;

import static javax.persistence.FetchType.LAZY;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class UserAchievement {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_achievement_id")
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "user_id")
    private UserInfo user;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "achievement_id")
    private Achievement achievement;

    public Long getAchievementId() {
        return achievement.getId();
    }

    public UserAchievement(UserInfo user, Achievement achievement) {
        this.user = user;
        this.achievement = achievement;
    }
}
