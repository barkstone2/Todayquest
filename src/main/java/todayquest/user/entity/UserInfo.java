package todayquest.user.entity;

import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;

import javax.persistence.*;
import java.time.LocalDateTime;

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

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

}
