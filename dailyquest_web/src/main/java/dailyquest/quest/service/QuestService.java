package dailyquest.quest.service;

import dailyquest.common.RestPage;
import dailyquest.quest.dto.*;
import dailyquest.quest.entity.QuestState;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
@RequiredArgsConstructor
@Service
public class QuestService {

    private final QuestQueryService questQueryService;
    private final QuestCommandService questCommandService;

    public List<QuestResponse> getCurrentQuests(Long userId, QuestState state) {
        return questQueryService.getCurrentQuests(userId, state);
    }

    public RestPage<QuestResponse> searchQuest(Long userId, QuestSearchCondition searchCondition, Pageable pageable) {
        return questQueryService.getQuestsByCondition(userId, searchCondition, pageable);
    }

    public RestPage<QuestResponse> searchQuest(List<Long> searchedIds, Pageable pageable) {
        return questQueryService.getSearchedQuests(searchedIds, pageable);
    }

    public QuestResponse getQuestInfo(Long questId, Long userId) {
        return QuestResponse.createDto(questQueryService.getEntityOfUser(questId, userId));
    }

    public QuestResponse saveQuest(QuestRequest dto, Long userId) {
        return questCommandService.saveQuest(dto, userId);
    }

    public QuestResponse updateQuest(QuestRequest dto, Long questId, Long userId) {
        return questCommandService.updateQuest(dto, questId, userId);
    }

    public QuestResponse deleteQuest(Long questId, Long userId) {
        return questCommandService.deleteQuest(questId, userId);
    }

    public QuestResponse completeQuest(Long questId, Long userId) {
        return questCommandService.completeQuest(questId, userId);
    }

    public QuestResponse discardQuest(Long questId, Long userId) {
        return questCommandService.discardQuest(questId, userId);
    }

    public DetailResponse updateDetailQuestCount(Long userId, DetailInteractRequest request) {
        return questCommandService.updateDetailQuestCount(userId, request);
    }

}
