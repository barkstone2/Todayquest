package dailyquest.quest.service;

import dailyquest.quest.dto.*;
import dailyquest.quest.entity.QuestState;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class QuestService {

    private final QuestQueryService questQueryService;
    private final QuestCommandService questCommandService;

    public List<QuestResponse> getCurrentQuests(Long userId, QuestState state) {
        return questQueryService.getCurrentQuests(userId, state);
    }

    public Page<QuestResponse> searchQuest(Long userId, QuestSearchCondition searchCondition, Pageable pageable) {
        return questQueryService.getQuestsByCondition(userId, searchCondition, pageable);
    }

    public Page<QuestResponse> searchQuest(List<Long> searchedIds, Pageable pageable) {
        return questQueryService.getSearchedQuests(searchedIds, pageable);
    }

    public QuestResponse getQuestInfo(Long questId, Long userId) {
        return questQueryService.getQuestInfo(questId, userId);
    }

    @Transactional
    public QuestResponse saveQuest(QuestRequest dto, Long userId) {
        return questCommandService.saveQuest(dto, userId);
    }

    @Transactional
    public QuestResponse updateQuest(QuestRequest dto, Long questId, Long userId) {
        return questCommandService.updateQuest(dto, questId, userId);
    }

    @Transactional
    public QuestResponse deleteQuest(Long questId, Long userId) {
        return questCommandService.deleteQuest(questId, userId);
    }

    @Transactional
    public QuestResponse completeQuest(Long userId, Long questId) {
        return questCommandService.completeQuest(userId, questId);
    }

    @Transactional
    public QuestResponse discardQuest(Long questId, Long userId) {
        return questCommandService.discardQuest(questId, userId);
    }

    @Transactional
    public DetailResponse updateDetailQuestCount(Long userId, DetailInteractRequest request) {
        return questCommandService.updateDetailQuestCount(userId, request);
    }
}
