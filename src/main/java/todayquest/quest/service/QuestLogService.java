package todayquest.quest.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import todayquest.quest.entity.QuestLog;
import todayquest.quest.entity.QuestState;
import todayquest.quest.repository.QuestLogRepository;

import java.util.Map;

@RequiredArgsConstructor
@Transactional
@Service
public class QuestLogService {
    private final QuestLogRepository questLogRepository;

    public void saveQuestLog(Long questId, Long userId, QuestState state) {
        questLogRepository.save(QuestLog.builder().questId(questId).userId(userId).state(state).build());
    }

    public Map<String, Long> getQuestLog(Long userId) {
        return questLogRepository.getQuestAnalytics(userId);
    }

}
