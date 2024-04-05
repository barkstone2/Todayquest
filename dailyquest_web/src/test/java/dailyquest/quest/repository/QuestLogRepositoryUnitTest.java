package dailyquest.quest.repository;

import dailyquest.config.JpaAuditingConfiguration;
import dailyquest.quest.dto.QuestLogSearchCondition;
import dailyquest.quest.dto.QuestStatisticsResponse;
import dailyquest.quest.entity.Quest;
import dailyquest.quest.entity.QuestLog;
import dailyquest.quest.entity.QuestState;
import dailyquest.quest.entity.QuestType;
import dailyquest.user.entity.ProviderType;
import dailyquest.user.entity.User;
import dailyquest.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@DisplayName("퀘스트 로그 리포지토리 유닛 테스트")
@DataJpaTest
@Import(JpaAuditingConfiguration.class)
public class QuestLogRepositoryUnitTest {

    @Autowired
    QuestLogRepository questLogRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    QuestRepository questRepository;

    @DisplayName("퀘스트 통계 조회 시 타입과 상태 별로 그룹화된 카운트가 반환된다")
    @Test
    public void testGetQuestLog() throws Exception {
        //given
        User user = new User("", "user", ProviderType.GOOGLE);
        User savedUser = userRepository.save(user);

        int completeCount = 3;
        int failCount = 2;
        int discardCount = 5;

        int mainCount = completeCount + failCount;

        LocalDate today = LocalDate.now();
        LocalTime mockTime = LocalTime.of(8, 0);

        try(MockedStatic<LocalTime> ignored = mockStatic(LocalTime.class, Answers.CALLS_REAL_METHODS)) {
            when(LocalTime.now()).thenReturn(mockTime);

            for (int i = 0; i < completeCount; i++) {
                Quest quest = new Quest("quest", "desc", savedUser, 1, QuestState.COMPLETE, QuestType.MAIN, null, null);
                Quest savedQuest = questRepository.save(quest);
                questLogRepository.save(new QuestLog(savedQuest));
            }

            for (int i = 0; i < failCount; i++) {
                Quest quest = new Quest("quest", "desc", savedUser, 1, QuestState.FAIL, QuestType.MAIN, null, null);
                Quest savedQuest = questRepository.save(quest);
                questLogRepository.save(new QuestLog(savedQuest));
            }

            for (int i = 0; i < discardCount; i++) {
                Quest quest = new Quest("quest", "desc", savedUser, 1, QuestState.DISCARD, QuestType.SUB, null, null);
                Quest savedQuest = questRepository.save(quest);
                questLogRepository.save(new QuestLog(savedQuest));
            }

            Quest quest = new Quest("quest", "desc", savedUser, 1, QuestState.PROCEED, QuestType.SUB, null, null);
            Quest savedQuest = questRepository.save(quest);
            questLogRepository.save(new QuestLog(savedQuest));

            //when
            List<QuestStatisticsResponse> groupedQuestLogs = questLogRepository.getGroupedQuestLogs(savedUser.getId(), new QuestLogSearchCondition());

            //then
            assertThat(groupedQuestLogs).allMatch(log -> log.getLoggedDate().equals(today));
            assertThat(groupedQuestLogs).anyMatch(log -> log.getCompleteCount() == completeCount);
            assertThat(groupedQuestLogs).anyMatch(log -> log.getFailCount() == failCount);
            assertThat(groupedQuestLogs).anyMatch(log -> log.getDiscardCount() == discardCount);
            assertThat(groupedQuestLogs).anyMatch(log -> log.getMainCount() == mainCount);
            assertThat(groupedQuestLogs).anyMatch(log -> log.getMainCount() == discardCount);
        }
    }

    @DisplayName("퀘스트 통계 조회 시 조회되는 값이 없으면 빈 리스트가 반환된다")
    @Test
    public void testEmptyQuestLog() throws Exception {
        //given
        User user = new User("", "user", ProviderType.GOOGLE);
        User savedUser = userRepository.save(user);

        //when
        List<QuestStatisticsResponse> groupedQuestLogs = questLogRepository.getGroupedQuestLogs(savedUser.getId(), new QuestLogSearchCondition());

        //then
        assertThat(groupedQuestLogs).isEmpty();
    }

}
