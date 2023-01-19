package todayquest.reward.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import todayquest.quest.entity.*;
import todayquest.quest.repository.QuestRepository;
import todayquest.quest.repository.QuestRewardRepository;
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

    @Autowired
    QuestRepository questRepository;

    @Autowired
    QuestRewardRepository questRewardRepository;

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
        assertThat(allByUserId.get(0).isDeleted()).isFalse();
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

    @DisplayName("삭제 상태의 리워드 목록 조회 테스트")
    @Test
    void testFindAllByUserIdDeleted() {
        //given
        rewardRepository.save(
                Reward.builder().name("1").description("1")
                        .grade(RewardGrade.E).user(userInfo)
                        .isDeleted(true).build()
        );

        //when
        List<Reward> allByUserId = rewardRepository.findAllByUserId(userInfo.getId());

        //then
        assertThat(allByUserId.size()).isEqualTo(0);
    }


    @DisplayName("단일 리워드 조회_정상")
    @Test
    void testFindByIdNotDeleted() {
        //given
        String rewardName = "reward name";
        Reward savedReward = rewardRepository.save(
                Reward.builder().name(rewardName).description("desc")
                        .grade(RewardGrade.E).user(userInfo)
                        .isDeleted(false).build()
        );

        //when
        Optional<Reward> findReward = rewardRepository.findByIdNotDeleted(savedReward.getId());

        //then
        assertThat(findReward).isNotEmpty();
        assertThat(findReward.get().getName()).isEqualTo(rewardName);
    }

    @DisplayName("단일 리워드 조회_삭제된 리워드")
    @Test
    void testFindByIdDeleted() {
        //given
        Reward savedReward = rewardRepository.save(
                Reward.builder().name("1").description("1")
                        .grade(RewardGrade.E).user(userInfo)
                        .isDeleted(true).build()
        );

        //when
        Optional<Reward> findReward = rewardRepository.findByIdNotDeleted(savedReward.getId());

        //then
        assertThat(findReward).isEmpty();
    }

    @DisplayName("진행 상태의 퀘스트에서 사용 여부 조회 테스트_사용중")
    @Test
    void testIsRewardUseInProceedQuestUsed() {
        //given
        Quest quest = Quest.builder()
                .title("title").description("desc")
                .difficulty(QuestDifficulty.EASY).isRepeat(true)
                .user(userInfo).state(QuestState.PROCEED)
                .type(QuestType.DAILY).seq(1L).build();
        Quest savedQuest = questRepository.save(quest);

        Reward reward = Reward.builder().name("1").description("1")
                .grade(RewardGrade.E).user(userInfo)
                .isDeleted(true).build();
        Reward savedReward = rewardRepository.save(reward);

        QuestReward qr = QuestReward.builder().quest(savedQuest).reward(savedReward).build();
        questRewardRepository.save(qr);

        //when
        boolean isUsed = rewardRepository.isRewardUseInProceedQuest(savedReward.getId());

        //then
        assertThat(isUsed).isTrue();
    }

    @DisplayName("진행 상태의 퀘스트에서 사용 여부 조회 테스트_사용안함")
    @Test
    void testIsRewardUseInProceedQuestNotUsed() {
        //given
        Reward reward = Reward.builder().name("1").description("1")
                .grade(RewardGrade.E).user(userInfo)
                .isDeleted(true).build();
        Reward savedReward = rewardRepository.save(reward);

        //when
        boolean isUsed = rewardRepository.isRewardUseInProceedQuest(savedReward.getId());

        //then
        assertThat(isUsed).isFalse();
    }


    @DisplayName("진행 상태의 퀘스트에서 사용 여부 조회 테스트_진행 상태 아님")
    @Test
    void testIsRewardUseInProceedQuestNotProceed() {
        //given
        Quest quest = Quest.builder()
                .title("title").description("desc")
                .difficulty(QuestDifficulty.EASY).isRepeat(true)
                .user(userInfo).state(QuestState.DISCARD)
                .type(QuestType.DAILY).seq(1L).build();
        Quest savedQuest = questRepository.save(quest);

        Reward reward = Reward.builder().name("1").description("1")
                .grade(RewardGrade.E).user(userInfo)
                .isDeleted(true).build();
        Reward savedReward = rewardRepository.save(reward);

        QuestReward qr = QuestReward.builder().quest(savedQuest).reward(savedReward).build();
        questRewardRepository.save(qr);

        //when
        boolean isUsed = rewardRepository.isRewardUseInProceedQuest(savedReward.getId());

        //then
        assertThat(isUsed).isFalse();
    }


    @DisplayName("reward ID 목록과 유저 ID로 조회")
    @Test
    void testFindAllByIdAndUserId() {
        //given
        Reward.RewardBuilder builder = Reward.builder()
                .name("1").description("1")
                .grade(RewardGrade.E).user(userInfo);

        Reward r1 = rewardRepository.save(builder.build());
        Reward r2 = rewardRepository.save(builder.build());
        Reward r3 = rewardRepository.save(builder.build());
        Reward r4 = rewardRepository.save(builder.build());

        //when
        List<Reward> result = rewardRepository.findAllByIdAndUserId(
                List.of(r1.getId(), r2.getId(), r3.getId()),
                userInfo.getId()
        );

        //then
        assertThat(result.size()).isEqualTo(3);
        assertThat(result).contains(r1, r2, r3);
        assertThat(result).doesNotContain(r4);
    }




}
