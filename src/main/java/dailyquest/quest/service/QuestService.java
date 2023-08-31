package dailyquest.quest.service;

import dailyquest.common.RestPage;
import dailyquest.quest.dto.DetailInteractRequest;
import dailyquest.quest.dto.DetailResponse;
import dailyquest.quest.dto.QuestRequest;
import dailyquest.quest.dto.QuestResponse;
import dailyquest.quest.entity.QuestState;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class QuestService {

    private final QuestQueryService questQueryService;
    private final QuestCommandService questCommandService;

    public RestPage<QuestResponse> getQuestList(Long userId, QuestState state, Pageable pageable) {
        return questQueryService.getQuestList(userId, state, pageable);
    }

    public QuestResponse getQuestInfo(Long questId, Long userId) {
        return questQueryService.getQuestInfo(questId, userId);
    }

    public QuestResponse saveQuest(QuestRequest dto, Long userId) {
        QuestResponse questResponse = questCommandService.saveQuest(dto, userId);
        return questResponse;
    }

    public QuestResponse updateQuest(QuestRequest dto, Long questId, Long userId) {
        QuestResponse questResponse = questCommandService.updateQuest(dto, questId, userId);
        return questResponse;
    }

    public void deleteQuest(Long questId, Long userId) {
        questCommandService.deleteQuest(questId, userId);
    }

    public void completeQuest(Long questId, Long userId) {
        questCommandService.completeQuest(questId, userId);
    }

    public void discardQuest(Long questId, Long userId) {
        questCommandService.discardQuest(questId, userId);
    }

    public DetailResponse interactWithDetailQuest(Long userId, Long questId, Long detailQuestId, DetailInteractRequest request) {
        return questCommandService.interactWithDetailQuest(userId, questId, detailQuestId, request);
    }
}
