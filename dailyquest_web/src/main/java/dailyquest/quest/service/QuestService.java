package dailyquest.quest.service;

import dailyquest.common.RestPage;
import dailyquest.quest.dto.*;
import dailyquest.quest.entity.QuestState;
import dailyquest.search.service.QuestIndexService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class QuestService {

    private final QuestQueryService questQueryService;
    private final QuestCommandService questCommandService;
    private final QuestIndexService questIndexService;

    public List<QuestResponse> getCurrentQuests(Long userId, QuestState state) {
        return questQueryService.getCurrentQuests(userId, state);
    }

    public RestPage<QuestResponse> searchQuest(Long userId, QuestSearchCondition searchCondition, Pageable pageable) {

        RestPage<QuestResponse> result;

        if(searchCondition.isKeywordSearch()) {
            List<Long> searchedIds = questIndexService.searchDocuments(searchCondition, userId, pageable);
            result = questQueryService.getSearchedQuests(searchedIds, userId, pageable);
        } else {
            result = questQueryService.getQuestsByCondition(userId, searchCondition, pageable);
        }

        return result;
    }

    public QuestResponse getQuestInfo(Long questId, Long userId) {
        return questQueryService.getQuestInfo(questId, userId);
    }

    public QuestResponse saveQuest(QuestRequest dto, Long userId) {
        QuestResponse questResponse = questCommandService.saveQuest(dto, userId);
        questIndexService.saveDocument(questResponse, userId);
        return questResponse;
    }

    public QuestResponse updateQuest(QuestRequest dto, Long questId, Long userId) {
        QuestResponse questResponse = questCommandService.updateQuest(dto, questId, userId);
        questIndexService.saveDocument(questResponse, userId);
        return questResponse;
    }

    public void deleteQuest(Long questId, Long userId) {
        questCommandService.deleteQuest(questId, userId);
        questIndexService.deleteDocument(questId);
    }

    public void completeQuest(Long questId, Long userId) {
        questCommandService.completeQuest(questId, userId);
        questIndexService.updateQuestStateOfDocument(questId, userId);
    }

    public void discardQuest(Long questId, Long userId) {
        questCommandService.discardQuest(questId, userId);
        questIndexService.updateQuestStateOfDocument(questId, userId);
    }

    public DetailResponse interactWithDetailQuest(Long userId, Long questId, Long detailQuestId, DetailInteractRequest request) {
        return questCommandService.interactWithDetailQuest(userId, questId, detailQuestId, request);
    }

}
