package todayquest.reward.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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


    @DisplayName("올바른 RewardId, 유저 ID로 리워드 조회")
    @Test
    void testFindByIdAndUserId() {
        //given
        Reward savedReward = rewardRepository.save(reward);

        //when
        Reward findReward = rewardRepository.findByIdAndUserId(savedReward.getId(), userInfo.getId()).get();

        //then
        assertThat(findReward.getName()).isEqualTo(savedReward.getName());
        assertThat(findReward.getId()).isEqualTo(savedReward.getId());
    }

    @DisplayName("잘못된 리워드 ID, 유저 ID로 리워드 조회")
    @Test
    void testFindByIdAndUserIdFail() {
        //given
        Reward savedReward = rewardRepository.save(reward);

        //when
        Optional<Reward> findReward1 = rewardRepository.findByIdAndUserId(savedReward.getId() + 1, userInfo.getId());
        Optional<Reward> findReward2 = rewardRepository.findByIdAndUserId(savedReward.getId(), userInfo.getId()+1);

        //then
        assertThat(findReward1).isEmpty();
        assertThat(findReward2).isEmpty();
    }


    @DisplayName("올바른 리워드 ID, 유저 ID로 리워드 삭제")
    @Test
    void deleteByIdAndUserId() {
        //given
        Reward savedReward = rewardRepository.save(reward);
        int beforeSize = rewardRepository.findAllByUserId(userInfo.getId()).size();

        //when
        rewardRepository.deleteByIdAndUserId(savedReward.getId(), userInfo.getId());

        //then
        int afterSize = rewardRepository.findAllByUserId(userInfo.getId()).size();

        assertThat(beforeSize).isEqualTo(afterSize+1);
    }

    @DisplayName("잘못된 리워드 ID, 유저 ID로 리워드 삭제")
    @Test
    void deleteByIdAndUserIdFail() {
        //given
        Reward savedReward = rewardRepository.save(reward);
        int beforeSize = rewardRepository.findAllByUserId(userInfo.getId()).size();

        //when
        rewardRepository.deleteByIdAndUserId(savedReward.getId()+1, userInfo.getId());
        rewardRepository.deleteByIdAndUserId(savedReward.getId(), userInfo.getId()+1);

        //then
        int afterSize = rewardRepository.findAllByUserId(userInfo.getId()).size();
        assertThat(beforeSize).isEqualTo(afterSize);
    }

}
