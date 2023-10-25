package dailyquest.quest.repository;

import dailyquest.quest.entity.Quest;
import dailyquest.quest.entity.QuestState;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface QuestRepositoryCustom {

    Long getNextSeqByUserId(Long userId);
    Page<Quest> findQuestsByCondition(Long userId, QuestState state, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
}
