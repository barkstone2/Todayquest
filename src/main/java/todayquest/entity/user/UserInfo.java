package todayquest.user.entity;

import lombok.*;
import todayquest.quest.entity.Quest;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor @Builder
@SequenceGenerator(name = "userIdGenerator", sequenceName = "USER_SEQUENCE")
@Entity
public class UserInfo {

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "userIdGenerator")
    @Column(name = "user_id")
    private Long id;

    @Column(nullable = false)
    private String oauth2Id;

    @Column(nullable = false, length = 20)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProviderType providerType;


    @OneToMany(mappedBy = "user")
    private List<Quest> quests = new ArrayList<>();

}
