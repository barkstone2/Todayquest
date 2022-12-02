package todayquest.achievement.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Achievement {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "achievement_id")
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 300)
    private String description;

    @Enumerated(EnumType.STRING)
    private AchievementType type;

    @Enumerated(EnumType.STRING)
    private AchievementAction action;

    @Column(nullable = false)
    private Long targetNumber;
}
