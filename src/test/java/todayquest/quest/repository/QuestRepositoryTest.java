package todayquest.quest.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import todayquest.quest.dto.QuestRequestDto;
import todayquest.quest.entity.*;
import todayquest.reward.entity.Reward;
import todayquest.reward.repository.RewardRepository;
import todayquest.user.entity.ProviderType;
import todayquest.user.entity.UserInfo;
import todayquest.user.repository.UserRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("퀘스트 리포지토리 유닛 테스트")
@DataJpaTest
class QuestRepositoryTest {

    @Autowired
    QuestRepository questRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RewardRepository rewardRepository;

    @Autowired
    QuestRewardRepository questRewardRepository;

    UserInfo userInfo;

    @BeforeEach
    void init() {
        userInfo = UserInfo.builder()
                .nickname("nickname")
                .providerType(ProviderType.GOOGLE)
                .oauth2Id("oauth2id").build();
        userRepository.save(userInfo);
    }

    @DisplayName("유저 별 퀘스트 목록 조회 종료 기한 오름차순 정렬")
    @Test
    public void testGetQuestsByUserOrderByDeadLineDateAscDeadLineTimeAsc() throws Exception {
        //given
        Quest quest1 = Quest.builder()
                .title("title")
                .description("desc")
                .difficulty(QuestDifficulty.easy)
                .state(QuestState.PROCEED)
                .type(QuestType.DAILY)
                .isRepeat(true)
                .deadLineDate(LocalDate.now())
                .deadLineTime(LocalTime.now())
                .user(userInfo)
                .build();

        Quest quest2 = Quest.builder()
                .title("title")
                .description("desc")
                .difficulty(QuestDifficulty.easy)
                .state(QuestState.PROCEED)
                .type(QuestType.DAILY)
                .isRepeat(true)
                .deadLineDate(LocalDate.now())
                .deadLineTime(LocalTime.now())
                .user(userInfo)
                .build();

        Quest quest3 = Quest.builder()
                .title("title")
                .description("desc")
                .difficulty(QuestDifficulty.easy)
                .state(QuestState.PROCEED)
                .type(QuestType.DAILY)
                .isRepeat(true)
                .deadLineDate(LocalDate.now())
                .deadLineTime(LocalTime.now())
                .user(userInfo)
                .build();

        questRepository.save(quest1);
        questRepository.save(quest2);
        questRepository.save(quest3);

        //when
        List<Quest> result = questRepository.getQuestsByUserOrderByDeadLineDateAscDeadLineTimeAsc(userInfo);

        //then
        assertThat(result).containsExactly(quest1, quest2, quest3);
        assertThat(result.size()).isEqualTo(3);
    }

    @DisplayName("퀘스트 보상 업데이트 테스트 기존 보상 제거")
    @Test
    void testUpdateRewardList1() {
        //given
        Quest quest1 = Quest.builder()
                .title("title")
                .description("desc")
                .difficulty(QuestDifficulty.easy)
                .state(QuestState.PROCEED)
                .type(QuestType.DAILY)
                .isRepeat(true)
                .deadLineDate(LocalDate.now())
                .deadLineTime(LocalTime.now())
                .user(userInfo)
                .build();

        questRepository.save(quest1);

        Reward reward1 = Reward.builder().name("1").build();
        Reward reward2 = Reward.builder().name("2").build();
        Reward reward3 = Reward.builder().name("3").build();

        List<Reward> rewards = new ArrayList<>();
        rewards.add(reward1);
        rewards.add(reward2);
        rewards.add(reward3);

        rewardRepository.saveAll(rewards);

        QuestReward qr1 = QuestReward.builder().quest(quest1).reward(reward1).build();
        QuestReward qr2 = QuestReward.builder().quest(quest1).reward(reward2).build();
        QuestReward qr3 = QuestReward.builder().quest(quest1).reward(reward3).build();

        questRewardRepository.save(qr1);
        questRewardRepository.save(qr2);
        questRewardRepository.save(qr3);

        Quest findQuest = questRepository.getById(quest1.getId());

        QuestRequestDto dto = QuestRequestDto.builder()
                .title("test title")
                .description("test description")
                .isRepeat(true)
                .deadLineDate(LocalDate.of(1111, 11, 11))
                .deadLineTime(LocalTime.of(11, 11))
                .difficulty(QuestDifficulty.easy)
                .build();

        rewards.remove(2);

        //when
        findQuest.updateQuestEntity(dto, rewards);

        //then
        assertThat(findQuest.getRewards().get(0).getReward().getName()).isEqualTo("1");
        assertThat(findQuest.getRewards().get(0).getReward().getId()).isEqualTo(reward1.getId());
        assertThat(findQuest.getRewards().size()).isEqualTo(2);
    }

    @DisplayName("퀘스트 보상 업데이트 테스트 신규 보상 추가")
    @Test
    void testUpdateRewardList2() {
        //given
        Quest quest1 = Quest.builder()
                .title("title")
                .description("desc")
                .difficulty(QuestDifficulty.easy)
                .state(QuestState.PROCEED)
                .type(QuestType.DAILY)
                .isRepeat(true)
                .deadLineDate(LocalDate.now())
                .deadLineTime(LocalTime.now())
                .user(userInfo)
                .build();

        List<Reward> rewards = new ArrayList<>();
        Reward reward1 = Reward.builder().name("1").build();
        Reward reward2 = Reward.builder().name("2").build();
        Reward reward3 = Reward.builder().name("3").build();
        rewards.add(reward1);
        rewards.add(reward2);
        rewards.add(reward3);

        rewardRepository.saveAll(rewards);

        questRewardRepository.save(QuestReward.builder().reward(reward1).quest(quest1).build());

        questRepository.save(quest1);

        Quest findQuest = questRepository.getById(quest1.getId());

        QuestRequestDto dto = QuestRequestDto.builder()
                .title("test title")
                .description("test description")
                .isRepeat(true)
                .deadLineDate(LocalDate.of(1111, 11, 11))
                .deadLineTime(LocalTime.of(11, 11))
                .difficulty(QuestDifficulty.easy)
                .build();


        //when
        findQuest.updateQuestEntity(dto, rewards);
        questRepository.flush();

        //then
        assertThat(findQuest.getRewards().size()).isEqualTo(3);
        assertThat(findQuest.getRewards().get(0).getReward().getId()).isEqualTo(reward1.getId());
        assertThat(findQuest.getRewards().get(2).getReward().getName()).isEqualTo("3");
    }


}