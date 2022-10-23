package todayquest.quest.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import todayquest.config.JpaAuditingConfiguration;
import todayquest.quest.entity.QuestLog;
import todayquest.quest.entity.QuestState;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("퀘스트 로그 리포지토리 단위 테스트")
@Import(JpaAuditingConfiguration.class)
@DataJpaTest
public class QuestLogRepositoryTest {

    @Autowired
    QuestLogRepository questLogRepository;


    @DisplayName("퀘스트 통계 조회 테스트")
    @Test
    public void testGetQuestAnalytics() throws Exception {
        //given
        Long userId = 1L;
        QuestLog ql1 = QuestLog.builder()
                .questId(1L).userId(userId)
                .state(QuestState.COMPLETE).build();
        QuestLog ql2 = QuestLog.builder()
                .questId(2L).userId(userId)
                .state(QuestState.COMPLETE).build();
        QuestLog ql3 = QuestLog.builder()
                .questId(3L).userId(userId)
                .state(QuestState.DISCARD).build();
        QuestLog ql4 = QuestLog.builder()
                .questId(4L).userId(userId)
                .state(QuestState.FAIL).build();

        questLogRepository.save(ql1);
        questLogRepository.save(ql2);
        questLogRepository.save(ql3);
        questLogRepository.save(ql4);

        //when
        Map<String, Long> questAnalytics = questLogRepository.getQuestAnalytics(userId);

        //then
        assertThat(questAnalytics.get(QuestState.COMPLETE.name())).isEqualTo(2);
        assertThat(questAnalytics.get(QuestState.DISCARD.name())).isEqualTo(1);
        assertThat(questAnalytics.get(QuestState.FAIL.name())).isEqualTo(1);
    }



}
