package todayquest.user.entity;

import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import todayquest.common.BaseTimeEntity;
import todayquest.quest.entity.QuestDifficulty;

import javax.persistence.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor @Builder
@DynamicInsert
@Entity
public class UserInfo extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(nullable = false)
    private String oauth2Id;

    @Column(nullable = false, length = 20)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProviderType providerType;

    private int level;
    private Long exp;
    private Long gold;

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }
    public void earnExpAndGold(QuestDifficulty clearInfo, Long targetExp) {
        this.gold += clearInfo.getGold();
        this.exp += clearInfo.getExperience();
        levelUpCheck(targetExp);
    }

    public void levelUpCheck(Long targetExp) {
        if(level == 100) return;
        if (exp >= targetExp) {
            level++;
            exp -= targetExp;
        }
    }

}
