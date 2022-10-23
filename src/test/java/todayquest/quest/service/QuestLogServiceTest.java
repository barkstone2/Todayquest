package todayquest.quest.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import todayquest.quest.entity.QuestLog;
import todayquest.quest.entity.QuestState;
import todayquest.quest.repository.QuestLogRepository;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@DisplayName("퀘스트 로그 서비스 테스트")
@ExtendWith(MockitoExtension.class)
public class QuestLogServiceTest {

    @InjectMocks
    QuestLogService questLogService;

    @Mock
    QuestLogRepository questLogRepository;

    @DisplayName("퀘스트 로그 저장 테스트")
    @Test
    public void testSaveQuestLog() throws Exception {
        //given
        Long questId = 1L;
        Long userId = 1L;
        QuestState state = QuestState.COMPLETE;

        //when
        questLogService.saveQuestLog(questId, userId, state);

        //then
        verify(questLogRepository).save(any(QuestLog.class));
    }


    @DisplayName("퀘스트 로그 조회 테스트")
    @Test
    public void testGetQuestLog() throws Exception {
        //given
        Long userId = 1L;

        //when
        questLogService.getQuestLog(userId);

        //then
        verify(questLogRepository).getQuestAnalytics(userId);
    }


}
