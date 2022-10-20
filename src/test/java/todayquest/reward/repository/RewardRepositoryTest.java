package todayquest.reward.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import todayquest.reward.entity.Reward;
import todayquest.reward.entity.RewardGrade;
import todayquest.user.entity.ProviderType;
import todayquest.user.entity.UserInfo;
import todayquest.user.repository.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("보상 아이템 리포지토리 유닛 테스트")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
public class RewardRepositoryTest {

    @Autowired
    RewardRepository rewardRepository;

    @Autowired
    UserRepository userRepository;

    UserInfo userInfo;
    Reward reward;

    @BeforeEach
    void init() {
        userInfo = UserInfo.builder()
                .nickname("nickname")
                .providerType(ProviderType.GOOGLE)
                .oauth2Id("oauth2id")
                .build();
        userRepository.save(userInfo);

        reward = Reward.builder()
                .name("1")
                .description("1")
                .grade(RewardGrade.E)
                .user(userInfo)
                .build();
    }

    @DisplayName("올바른 유저 ID로 리워드 리스트 조회")
    @Test
    void testFindAllByUserIdSuccess() {
        //given
        rewardRepository.save(reward);

        //when
        List<Reward> allByUserId = rewardRepository.findAllByUserId(userInfo.getId());

        //then
        assertThat(allByUserId.size()).isEqualTo(1);
        assertThat(allByUserId.get(0).getName()).isEqualTo(reward.getName());
    }

    @DisplayName("잘못된 유저 ID로 리워드 리스트 조회")
    @Test
    void testFindAllByUserIdFail() {
        //given
        rewardRepository.save(reward);

        //when
        List<Reward> allByUserId = rewardRepository.findAllByUserId(userInfo.getId()+1);

        //then
        assertThat(allByUserId.size()).isEqualTo(0);
    }


}
