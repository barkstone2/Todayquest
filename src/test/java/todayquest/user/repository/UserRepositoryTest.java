package todayquest.user.repository;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import todayquest.user.entity.ProviderType;
import todayquest.user.entity.UserInfo;

import java.util.List;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("유저 리포지토리 테스트")
@Slf4j
@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;
    UserInfo userInfo;

    @BeforeEach
    void init() {
        userInfo = UserInfo.builder()
                .id(1L)
                .nickname("nickname")
                .oauth2Id("oid")
                .providerType(ProviderType.GOOGLE)
                .build();

    }

    @DisplayName("Oauth2Id로 유저 정보 조회 테스트")
    @Test
    void testFindByOauth2Id() {
        //given
        String oauth2Id = "oid";

        //when
        UserInfo findUser = userRepository.findByOauth2Id(oauth2Id);

        //then
        assertThat(findUser).isNotNull();
        assertThat(findUser.getId()).isEqualTo(1L);
    }

    @DisplayName("닉네임 중복 체크 테스트")
    @Test
    void testExistsByNickname() {
        //given
        UserInfo savedUser = userRepository.getById(1L);
        String duplicateNickname = savedUser.getNickname();
        String newNickname = "newNickname";

        //when
        boolean isDuplicate = userRepository.existsByNickname(duplicateNickname);
        boolean isDuplicateNewNickname = userRepository.existsByNickname(newNickname);

        //then
        assertThat(isDuplicate).isTrue();
        assertThat(isDuplicateNewNickname).isFalse();
    }
}