package dailyquest.quest.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import dailyquest.quest.dto.QuestLogSearchCondition;
import dailyquest.quest.entity.Quest;
import dailyquest.quest.entity.QuestLog;
import dailyquest.quest.repository.QuestLogRepository;

import java.time.LocalDate;
import java.util.Map;

@RequiredArgsConstructor
@Transactional
@Service
public class QuestLogService {
    private final QuestLogRepository questLogRepository;

    public void saveQuestLog(Quest quest) {
        questLogRepository.save(new QuestLog(quest));
    }

    public Map<String, Map> getQuestStatistic(Long userId, QuestLogSearchCondition condition) {
        Map<LocalDate, Map<String, Long>> questStatisticByState = questLogRepository.getQuestStatisticByState(userId, condition);

        for (Map<String, Long> stateMap : questStatisticByState.values()) {
            stateMap.compute("RATIO", (key, value)-> {
                long allQuestCount = stateMap.values().stream().mapToLong(v -> v).sum();
                Long completeCount = stateMap.get("COMPLETE");

                if(allQuestCount == 0) return 0L;

                double ratio = (double) completeCount / allQuestCount;
                double percent = ratio * 100;
                return Math.round(percent);
            });
        }

        Map<LocalDate, Map<String, Long>> questStatisticByType = questLogRepository.getQuestStatisticByType(userId, condition);

        for (Map<String, Long> typeMap : questStatisticByType.values()) {
            typeMap.compute("RATIO", (key, value) -> {
                long main = typeMap.get("MAIN");
                long sub = typeMap.get("SUB");

                if (main + sub == 0) {
                    return 0L;
                }

                double ratio = (double) main / (main + sub);
                double percent = ratio * 100;
                return Math.round(percent);
            });
        }

        return Map.of("state", questStatisticByState, "type", questStatisticByType);
    }

}
