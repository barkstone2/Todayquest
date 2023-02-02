package todayquest.quest.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import todayquest.quest.entity.QuestLog;
import todayquest.quest.dto.QuestLogSearchCondition;
import todayquest.quest.repository.QuestLogRepository;
import todayquest.quest.entity.Quest;

import java.util.Map;

@RequiredArgsConstructor
@Transactional
@Service
public class QuestLogService {
    private final QuestLogRepository questLogRepository;

    public void saveQuestLog(Quest quest) {
        questLogRepository.save(new QuestLog(quest));
    }

    public Map<String, Map<String, Long>> getQuestStatistic(Long userId, QuestLogSearchCondition condition) {
        Map<String, Long> questStatisticByState = questLogRepository.getQuestStatisticByState(userId, condition);
        Map<String, Long> questStatisticByType = questLogRepository.getQuestStatisticByType(userId, condition);

        questStatisticByType.compute("RATIO", (key, value) -> {
            long main = questStatisticByType.get("MAIN");
            long sub = questStatisticByType.get("SUB");

            if (main + sub == 0) {
                return 0L;
            }

            double ratio = (double) main / (main + sub);
            double percent = ratio * 100;
            return Math.round(percent);
        });

        return Map.of("state", questStatisticByState, "type", questStatisticByType);
    }

}
