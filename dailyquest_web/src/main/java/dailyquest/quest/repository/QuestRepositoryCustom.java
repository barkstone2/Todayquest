package dailyquest.quest.repository;

import dailyquest.quest.dto.QuestSearchCondition;
import dailyquest.quest.entity.Quest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface QuestRepositoryCustom {

    Long getNextSeqByUserId(Long userId);
    Page<Quest> findQuestsByCondition(Long userId, QuestSearchCondition searchCondition, Pageable pageable);
}
