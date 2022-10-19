package todayquest.user.repository;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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
                .nickname("oldNickname")
                .oauth2Id("oauth2-id")
                .providerType(ProviderType.GOOGLE)
                .build();

    }


    @DisplayName("신규 유저 등록 테스트")
    @Test
    public void saveUserTest() throws Exception {
        //given
        UserInfo newUser = UserInfo.builder()
                .nickname("newUser")
                .oauth2Id("oauth2-id-save")
                .providerType(ProviderType.GOOGLE)
                .build();

        //when
        UserInfo savedUser = userRepository.save(newUser);

        //then
        assertThat(newUser.getId()).isEqualTo(savedUser.getId());
    }

    @DisplayName("유저 목록 조회 테스트")
    @Test
    public void testFindAll() throws Exception {
        //given
        UserInfo user1 = UserInfo.builder().nickname("nick-list-1").oauth2Id("oauth2-id-list-1")
                .providerType(ProviderType.GOOGLE).build();
        UserInfo user2 = UserInfo.builder().nickname("nick-list-2").oauth2Id("oauth2-id-list-2")
                .providerType(ProviderType.GOOGLE).build();
        UserInfo user3 = UserInfo.builder().nickname("nick-list-3").oauth2Id("oauth2-id-list-3")
                .providerType(ProviderType.GOOGLE).build();
        userRepository.save(user1);
        userRepository.save(user2);
        userRepository.save(user3);

        //when
        List<UserInfo> all = userRepository.findAll();

        //then
        assertThat(all.size()).isEqualTo(3);
        assertThat(all).containsExactly(user1, user2, user3);
    }

    @DisplayName("유저 정보 조회 테스트")
    @Test
    public void testFindUserById() throws Exception {
        //given
        UserInfo savedUser = userRepository.save(userInfo);

        //when
        UserInfo findUser = userRepository.findById(savedUser.getId()).get();

        //then
        assertThat(findUser.getNickname()).isEqualTo(savedUser.getNickname());
    }


    @DisplayName("더티체킹을 통한 유저 정보 업데이트 테스트")
    @Test
    public void testUpdateUser() throws Exception {
        //given
        UserInfo savedUser = userRepository.save(userInfo);
        String newNickname = "newNickname";

        //when
        savedUser.updateNickname(newNickname);
        userRepository.flush();

        UserInfo findUser = userRepository.findById(savedUser.getId()).get();

        //then
        assertThat(findUser.getNickname()).isEqualTo(newNickname);
    }


    @DisplayName("유저 정보 삭제 테스트")
    @Test
    public void testDeleteUserById() throws Exception {
        //given
        UserInfo savedUser = userRepository.save(userInfo);
        Long userId = savedUser.getId();

        //when
        userRepository.deleteById(userId);

        //then
        assertThatThrownBy(() -> userRepository.findById(userId).get())
                .isInstanceOf(NoSuchElementException.class);
    }


    @DisplayName("Oauth2Id로 유저 정보 조회 테스트")
    @Test
    void testFindByOauth2Id() {
        //given
        UserInfo savedUser = userRepository.save(userInfo);

        //when
        UserInfo findUser = userRepository.findByOauth2Id(savedUser.getOauth2Id());

        //then
        assertThat(savedUser.getId()).isEqualTo(findUser.getId());
    }

    @DisplayName("닉네임 중복 체크 테스트")
    @Test
    void testExistsByNickname() {
        //given
        UserInfo savedUser = userRepository.save(userInfo);
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