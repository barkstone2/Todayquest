package todayquest.quest.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import todayquest.quest.entity.Quest;
import todayquest.quest.entity.QuestDifficulty;
import todayquest.quest.entity.QuestState;
import todayquest.quest.entity.QuestType;
import todayquest.reward.repository.RewardRepository;
import todayquest.user.entity.ProviderType;
import todayquest.user.entity.UserInfo;
import todayquest.user.repository.UserRepository;

import java.time.LocalDate;
import java.time.LocalTime;

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
    UserInfo anotherUser;

    @BeforeEach
    void init() {
        userInfo = UserInfo.builder()
                .nickname("nickname")
                .providerType(ProviderType.GOOGLE)
                .oauth2Id("oauth2id").build();
        anotherUser = UserInfo.builder()
                .nickname("nickname2")
                .providerType(ProviderType.GOOGLE)
                .oauth2Id("oauth2id").build();
        userInfo = userRepository.save(userInfo);
        anotherUser = userRepository.save(anotherUser);
    }

    @DisplayName("퀘스트 목록 조회")
    @Test
    public void testGetQuestsList() throws Exception {
        //given

        Quest.QuestBuilder builder = Quest.builder()
                .title("title")
                .description("desc")
                .difficulty(QuestDifficulty.EASY)
                .state(QuestState.PROCEED)
                .type(QuestType.DAILY)
                .deadLineDate(LocalDate.of(2000, 10, 10))
                .deadLineTime(LocalTime.of(6, 6, 6))
                .user(userInfo)
                .isRepeat(true)
                .seq(1L);


        // 정상 등록 2건
        Quest savedQuest1 = questRepository.save(builder.build());
        Quest savedQuest2 = questRepository.save(builder.build());

        // PROCEED 상태가 아닌 퀘스트 1건 등록
        Quest savedQuest3 = questRepository.save(builder.state(QuestState.DISCARD).build());

        // 다른 유저의 퀘스트 1건 등록
        Quest savedQuest4 = questRepository.save(builder.user(anotherUser).build());

        // deadLineDate == null 인 퀘스트 1건 등록
        Quest savedQuest5 = questRepository.save(Quest.builder()
                .title("title")
                .description("desc")
                .difficulty(QuestDifficulty.EASY)
                .state(QuestState.PROCEED)
                .type(QuestType.DAILY)
                .user(userInfo)
                .deadLineTime(LocalTime.of(6, 6, 6))
                .isRepeat(true)
                .seq(1L).build());

        //when
        Slice<Quest> result = questRepository.getQuestsList(userInfo.getId(), QuestState.PROCEED, PageRequest.of(0, 9));

        //then
        assertThat(result.getContent()).contains(savedQuest1, savedQuest2, savedQuest5);
        assertThat(result.getContent()).doesNotContain(savedQuest3, savedQuest4);
        assertThat(result.getContent().indexOf(savedQuest5)).isEqualTo(result.getContent().size()-1);
        assertThat(result.getContent().size()).isEqualTo(3);
    }

    @DisplayName("퀘스트 SEQ 조회 단순 로직 테스트")
    @Test
    public void testGetMaxSeqMethod() throws Exception {
        //given
        Quest quest = Quest.builder()
                .title("title")
                .description("desc")
                .difficulty(QuestDifficulty.EASY)
                .state(QuestState.PROCEED)
                .type(QuestType.DAILY)
                .deadLineDate(LocalDate.of(2000, 10, 10))
                .deadLineTime(LocalTime.of(6, 6, 6))
                .user(userInfo)
                .isRepeat(true)
                .seq(10L)
                .build();
        Quest savedQuest = questRepository.save(quest);

        //when
        Long currentSeq = savedQuest.getSeq();
        Long nextSeq = questRepository.getNextSeqByUserId(userInfo.getId());
        Long anotherUserSeq = questRepository.getNextSeqByUserId(anotherUser.getId());

        //then
        assertThat(nextSeq).isEqualTo(currentSeq+1);
        assertThat(anotherUserSeq).isEqualTo(1);
    }

}