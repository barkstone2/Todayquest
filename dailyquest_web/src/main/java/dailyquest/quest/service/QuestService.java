package dailyquest.quest.service;

import dailyquest.common.RestPage;
import dailyquest.quest.dto.*;
import dailyquest.quest.entity.QuestState;
import dailyquest.search.service.QuestIndexService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

/* TODO
    다른 서비스를 통한 작업이 추가되고 트랜잭션을 필요로 할 경우
    반드시 비동기 로직을 트랜잭션 밖으로 분리하거나
    비동기 로직 호출 전에 flush를 호출해 트랜잭션 커밋 중에 발생하는 오류로 인한
    데이터 불일치를 방지해야함
*/
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
        QuestResponse deletedQuest = questCommandService.deleteQuest(questId, userId);
        questIndexService.deleteDocument(deletedQuest);
    }

    public void completeQuest(Long questId, Long userId) {
        QuestResponse questResponse = questCommandService.completeQuest(questId, userId);
        questIndexService.updateQuestStateOfDocument(questResponse, userId);
    }

    public void discardQuest(Long questId, Long userId) {
        QuestResponse questResponse = questCommandService.discardQuest(questId, userId);
        questIndexService.updateQuestStateOfDocument(questResponse, userId);
    }

    public DetailResponse interactWithDetailQuest(Long userId, DetailInteractRequest request) {
        return questCommandService.interactWithDetailQuest(userId, request);
    }

}
