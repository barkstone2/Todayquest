package todayquest.user.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import todayquest.user.entity.ProviderType;
import todayquest.user.entity.UserInfo;

import static org.assertj.core.api.Assertions.*;

class UserPrincipalTest {

    private UserInfo userInfo;

    @BeforeEach
    void init() {
        userInfo = UserInfo.builder()
                .id(1L)
                .providerType(ProviderType.GOOGLE)
                .nickname("test")
                .oauth2Id("test-id-1111")
                .build();
    }

    @DisplayName("기본 권한을 부여한 UserPrincipal 객체 생성 테스트")
    @Test
    void testCreateUserPrincipal() {
        UserPrincipal userPrincipal = UserPrincipal.create(userInfo);

        assertThat(userPrincipal.getUserId()).isEqualTo(userInfo.getId());
        assertThat(userPrincipal.getNickname()).isEqualTo(userInfo.getNickname());
        assertThat(userPrincipal.getProviderType()).isEqualTo(userInfo.getProviderType());
        assertThat(userPrincipal.getAuthorities()).element(0).isInstanceOf(SimpleGrantedAuthority.class);
        assertThat(userPrincipal.getAuthorities()).allMatch(ga -> ga.getAuthority().equals("ROLE_USER"));
    }

}