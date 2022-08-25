package todayquest.quest.entity;

import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import todayquest.quest.dto.QuestRequestDto;
import todayquest.user.entity.UserInfo;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static javax.persistence.FetchType.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor @Builder
@Entity
public class Quest {

    @Id @GeneratedValue
    @Column(name = "quest_id")
    private Long id;

    @Column(length = 50, nullable = false)
    private String title;

    @Column(length = 300)
    private String description;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "user_id")
    private UserInfo user;

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
    @Column(nullable = false)
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

    /**
     * 등록자 정보는 추후 User 엔티티 추가 후 해당 정보를 받아서 처리할 것
     */
    private String regUser;
    private String regIp;
    private LocalDateTime regDate;
    private LocalDateTime modifyDate;

    public void updateQuestEntity(QuestRequestDto dto) {
        this.title = dto.getTitle();
        this.description = dto.getDescription();
        this.isRepeat = dto.isRepeat();
        this.deadLineDate = dto.getDeadLineDate();
        this.deadLineTime = dto.getDeadLineTime();
        this.difficulty = dto.getDifficulty();
    }
}

