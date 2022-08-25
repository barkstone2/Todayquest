package todayquest.quest.repository;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.security.core.userdetails.User;
import todayquest.quest.entity.Quest;
import todayquest.quest.entity.QuestState;
import todayquest.quest.entity.QuestType;
import todayquest.user.entity.ProviderType;
import todayquest.user.entity.UserInfo;
import todayquest.user.repository.UserRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
class QuestRepositoryTest {

    @Autowired
    QuestRepository questRepository;

    @Autowired
    UserRepository userRepository;

    @BeforeEach
    void init() {
        UserInfo userInfo = UserInfo.builder()
                .id(1L).nickname("nickname").providerType(ProviderType.GOOGLE)
                .oauth2Id("oauth2id").build();
        userRepository.save(userInfo);
    }

    @Test
    public void testGetQuestsByUserOrderByDeadLineDateAscDeadLineTimeAsc() throws Exception {
        //given
        UserInfo userInfo = UserInfo.builder()
                .id(1L).nickname("nickname").providerType(ProviderType.GOOGLE)
                .oauth2Id("oauth2id").build();
        userRepository.save(userInfo);

        Quest quest1 = Quest.builder()
                .title("title").description("desc")
                .deadLineTime(LocalTime.now()).deadLineDate(LocalDate.now())
                .state(QuestState.PROCEED).isRepeat(true).type(QuestType.DAILY)
                .user(userInfo)
                .build();
        Quest quest2 = Quest.builder()
                .title("title").description("desc")
                .deadLineTime(LocalTime.now()).deadLineDate(LocalDate.now())
                .state(QuestState.PROCEED).isRepeat(true).type(QuestType.DAILY)
                .user(userInfo)
                .build();
        Quest quest3 = Quest.builder()
                .title("title").description("desc")
                .deadLineTime(LocalTime.now()).deadLineDate(LocalDate.now())
                .state(QuestState.PROCEED).isRepeat(true).type(QuestType.DAILY)
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
}