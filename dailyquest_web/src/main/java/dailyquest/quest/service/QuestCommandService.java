package dailyquest.quest.service;

import dailyquest.common.MessageUtil;
import dailyquest.quest.dto.*;
import dailyquest.quest.entity.DetailQuest;
import dailyquest.quest.entity.Quest;
import dailyquest.quest.entity.QuestState;
import dailyquest.quest.repository.QuestRepository;
import dailyquest.user.entity.User;
import dailyquest.user.repository.UserRepository;
import dailyquest.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Transactional
@Service
public class QuestCommandService {
    private final QuestQueryService questQueryService;

    private final QuestRepository questRepository;
    private final UserRepository userRepository;

    private final UserService userService;
    private final QuestLogService questLogService;

    public QuestResponse saveQuest(QuestRequest dto, Long userId) {
        User findUser = userRepository.getReferenceById(userId);
        dto.checkRangeOfDeadLine();

        if (findUser.isNowCoreTime()) {
            dto.toMainQuest();
        }

        Long nextSeq = questRepository.getNextSeqByUserId(userId);

        Quest quest = dto.mapToEntity(nextSeq, findUser);
        questRepository.save(quest);
        QuestLogRequest questLogRequest = QuestLogRequest.from(quest);
        questLogService.saveQuestLog(questLogRequest);
        return QuestResponse.createDto(quest);
    }

    public QuestResponse updateQuest(QuestRequest dto, Long questId, Long userId) {
        Quest quest = questQueryService.getProceedEntityOfUser(questId, userId);
        dto.checkRangeOfDeadLine();
        if(quest.isMainQuest()) dto.toMainQuest();

        List<DetailQuest> details = dto.getDetails().stream().map(detail -> detail.mapToEntity(quest)).toList();
        quest.updateQuestEntity(dto.getTitle(), dto.getDescription(), dto.getDeadLine(), details);

        return QuestResponse.createDto(quest);
    }

    public QuestResponse deleteQuest(Long questId, Long userId) {
        Quest quest = questQueryService.getEntityOfUser(questId, userId);
        quest.deleteQuest();
        return QuestResponse.createDto(quest);
    }

    public QuestResponse completeQuest(Long userId, QuestCompletionRequest questCompletionRequest) {
        Quest quest = questQueryService.getEntityOfUser(questCompletionRequest.getQuestId(), userId);
        QuestState resultState = quest.completeQuest();
        switch (resultState) {
            case COMPLETE -> {
                if (quest.isMainQuest()) {
                    questCompletionRequest.toMainQuest();
                }
                userService.addUserExpAndGold(userId, questCompletionRequest);
                QuestLogRequest questLogRequest = QuestLogRequest.from(quest);
                questLogService.saveQuestLog(questLogRequest);
            }
            case DELETE -> throw new IllegalStateException(MessageUtil.getMessage("quest.error.deleted"));
            case PROCEED -> throw new IllegalStateException(MessageUtil.getMessage("quest.error.complete.detail"));
            default -> throw new IllegalStateException(MessageUtil.getMessage("quest.error.not-proceed"));
        }
        return QuestResponse.createDto(quest);
    }

    public QuestResponse discardQuest(Long questId, Long userId) {
        Quest quest = questQueryService.getEntityOfUser(questId, userId);
        QuestState resultState = quest.discardQuest();
        switch (resultState) {
            case DISCARD -> {
                QuestLogRequest questLogRequest = QuestLogRequest.from(quest);
                questLogService.saveQuestLog(questLogRequest);
            }
            case DELETE -> throw new IllegalStateException(MessageUtil.getMessage("quest.error.deleted"));
            default -> throw new IllegalStateException(MessageUtil.getMessage("quest.error.not-proceed"));
        }
        return QuestResponse.createDto(quest);
    }

    public DetailResponse updateDetailQuestCount(Long userId, DetailInteractRequest request) {
        Quest quest = questQueryService.getProceedEntityOfUser(request.getQuestId(), userId);
        DetailQuest interactResult = quest.updateDetailQuestCount(request.getDetailQuestId(), request.getCount());
        return DetailResponse.of(interactResult, quest.canComplete());
    }
}
