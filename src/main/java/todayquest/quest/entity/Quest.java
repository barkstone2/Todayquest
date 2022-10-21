package todayquest.quest.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import todayquest.common.BaseTimeEntity;
import todayquest.quest.dto.QuestRequestDto;
import todayquest.reward.entity.Reward;
import todayquest.user.entity.UserInfo;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static javax.persistence.FetchType.LAZY;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Quest extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "quest_id")
    private Long id;

    @Column(length = 50, nullable = false)
    private String title;

    @Column(length = 300)
    private String description;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "user_id")
    private UserInfo user;

    @Column(name = "user_quest_seq")
    private Long seq;

    /**
     * QuestType 정보와 조합해 반복 주기를 설정한다.
     * 마찬가지로 배치 처리가 필요할 듯 하다.
     */
    @Column(nullable = false)
    private boolean isRepeat;

    /**
     * 현재 일간 퀘스트만 사용하므로 시간 정보만 받아서 처리, 날짜 정보는 앞단에서 오늘 날짜와 동일하게 처리한다.
     * 추후 주간, 월간, 연간 퀘스트 추가 시 이 필드에 날짜 정보를 입력 받아 배치 처리에 사용한다.
     */
    private LocalDate deadLineDate;
    private LocalTime deadLineTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuestState state;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuestType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuestDifficulty difficulty;

    @OneToMany(mappedBy = "quest")
    private List<QuestReward> rewards = new ArrayList<>();


    @Builder
    public Quest(String title, String description, UserInfo user, Long seq, boolean isRepeat, LocalDate deadLineDate, LocalTime deadLineTime,
                 QuestState state, QuestType type, QuestDifficulty difficulty) {

        this.title = title;
        this.description = description;
        this.user = user;
        this.seq = seq;
        this.isRepeat = isRepeat;
        this.deadLineDate = deadLineDate;
        this.deadLineTime = deadLineTime;
        this.state = state;
        this.type = type;
        this.difficulty = difficulty;
    }

    public List<QuestReward> updateQuestEntity(QuestRequestDto dto, List<Reward> updateRewards) {
        this.title = dto.getTitle();
        this.description = dto.getDescription();
        this.isRepeat = dto.isRepeat();
        this.deadLineDate = dto.getDeadLineDate();
        this.deadLineTime = dto.getDeadLineTime();
        this.difficulty = dto.getDifficulty();
        return updateRewardList(updateRewards);
    }

    private List<QuestReward> updateRewardList(List<Reward> updateRewards) {
        List<QuestReward> newRewards = new ArrayList<>();

        int updateCount = updateRewards.size();

        for (int i = 0; i < updateCount; i++) {
            QuestReward newReward = QuestReward.builder().reward(updateRewards.get(i)).quest(this).build();
            try {
                rewards.get(i).updateReward(updateRewards.get(i));
            } catch (IndexOutOfBoundsException e) {
                newRewards.add(newReward);
            }
        }

        int overCount = rewards.size() - updateCount;
        if (overCount > 0) {
            for (int i = updateCount; i < updateCount + overCount; i++) {
                rewards.remove(i);
            }
        }

        return newRewards;
    }

    public void changeState(QuestState state) {
        this.state = state;
    }

}

