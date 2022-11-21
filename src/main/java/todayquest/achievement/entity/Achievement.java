package todayquest.achievement.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class Achievement {

    @Id @GeneratedValue
    @Column(name = "achievement_id")
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 300)
    private String description;

    private AchievementType type;
    private AchievementAction action;

    @Column(nullable = false)
    private Long targetNumber;
}
