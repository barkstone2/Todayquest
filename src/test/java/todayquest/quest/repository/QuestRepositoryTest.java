package todayquest.quest.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import todayquest.quest.dto.QuestRequestDto;
import todayquest.quest.entity.*;
import todayquest.user.entity.DifficultyType;
import todayquest.user.entity.ProviderType;
import todayquest.user.entity.UserInfo;
import todayquest.user.repository.UserRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class QuestRepositoryTest {

    @Autowired
    QuestRepository questRepository;

    @Autowired
    UserRepository userRepository;

    UserInfo userInfo;

    @BeforeEach
    void init() {
        userInfo = UserInfo.builder()
                .nickname("nickname").providerType(ProviderType.GOOGLE)
                .difficultyType(DifficultyType.difficulty)
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
                .rewards(new ArrayList<>())
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
                .rewards(new ArrayList<>())
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
                .rewards(new ArrayList<>())
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
                .rewards(new ArrayList<>())
                .user(userInfo)
                .build();

        QuestReward reward1 = QuestReward.builder().reward("1").build();
        QuestReward reward2 = QuestReward.builder().reward("2").build();
        QuestReward reward3 = QuestReward.builder().reward("3").build();

        quest1.getRewards().add(reward1);
        quest1.getRewards().add(reward2);
        quest1.getRewards().add(reward3);

        questRepository.save(quest1);

        Quest findQuest = questRepository.getById(quest1.getId());

        QuestRequestDto dto = QuestRequestDto.builder()
                .title("test title")
                .description("test description")
                .isRepeat(true)
                .deadLineDate(LocalDate.of(1111, 11, 11))
                .deadLineTime(LocalTime.of(11, 11))
                .difficulty(QuestDifficulty.easy)
                .rewards(List.of("reward1", "reward2"))
                .build();

        //when
        findQuest.updateQuestEntity(dto);

        //then
        assertThat(findQuest.getRewards().get(0).getReward()).isEqualTo("reward1");
        assertThat(findQuest.getRewards().get(0).getId()).isEqualTo(reward1.getId());
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
                .rewards(new ArrayList<>())
                .user(userInfo)
                .build();

        QuestReward reward1 = QuestReward.builder().reward("1").build();
        QuestReward reward2 = QuestReward.builder().reward("2").build();

        quest1.getRewards().add(reward1);
        quest1.getRewards().add(reward2);

        questRepository.save(quest1);

        Quest findQuest = questRepository.getById(quest1.getId());

        QuestRequestDto dto = QuestRequestDto.builder()
                .title("test title")
                .description("test description")
                .isRepeat(true)
                .deadLineDate(LocalDate.of(1111, 11, 11))
                .deadLineTime(LocalTime.of(11, 11))
                .difficulty(QuestDifficulty.easy)
                .rewards(List.of("reward1", "reward2", "reward3", "reward4"))
                .build();

        //when
        findQuest.updateQuestEntity(dto);
        questRepository.flush();

        //then
        assertThat(findQuest.getRewards().size()).isEqualTo(4);
        assertThat(findQuest.getRewards().get(0).getId()).isEqualTo(reward1.getId());
        assertThat(findQuest.getRewards().get(3).getReward()).isEqualTo("reward4");
        assertThat(findQuest.getRewards().get(3).getId()).isNotNull();
    }


}