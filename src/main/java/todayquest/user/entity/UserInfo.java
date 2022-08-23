package todayquest.user.entity;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor @Builder
@SequenceGenerator(name = "userIdGenerator", sequenceName = "USER_SEQUENCE")
@Entity
public class UserInfo {

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "userIdGenerator")
    private Long id;

    @Column(nullable = false)
    private String oauth2Id;

    @Column(nullable = false, length = 20)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProviderType providerType;

}
