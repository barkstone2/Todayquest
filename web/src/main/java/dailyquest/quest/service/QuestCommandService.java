package dailyquest.quest.service;

import dailyquest.quest.dto.*;
import dailyquest.quest.entity.DetailQuest;
import dailyquest.quest.entity.Quest;
import dailyquest.quest.repository.QuestRepository;
import dailyquest.redis.service.RedisService;
import dailyquest.user.record.service.UserRecordService;
import dailyquest.user.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional
@Service
public class QuestCommandService {
    private final QuestRepository questRepository;
    private final UserService userService;
    private final UserRecordService userRecordService;
    private final QuestLogService questLogService;
    private final RedisService redisService;
    private final MessageSourceAccessor messageSourceAccessor;

    public QuestResponse saveQuest(WebQuestRequest dto, Long userId) {
        Long nextSeq = questRepository.getNextSeqOfUser(userId);
        Quest quest = dto.mapToEntity(nextSeq, userId);
        questRepository.saveAndFlush(quest);
        QuestLogRequest questLogRequest = QuestLogRequest.from(quest);
        questLogService.saveQuestLog(questLogRequest);
        userRecordService.recordQuestRegistration(userId, questLogRequest.getLoggedDate());
        return QuestResponse.createDto(quest);
    }

    public QuestResponse updateQuest(WebQuestRequest updateRequest, Long questId, Long userId) {
        Quest quest = this.getProceedEntityOfUser(questId, userId);
        quest.updateQuestEntity(updateRequest);
        return QuestResponse.createDto(quest);
    }

    private Quest getProceedEntityOfUser(Long questId, Long userId) {
        Quest quest = this.getEntityOfUser(questId, userId);
        if(!quest.isProceed())
            throw new IllegalStateException(messageSourceAccessor.getMessage("quest.error.not-proceed"));
        return quest;
    }

    private Quest getEntityOfUser(Long questId, Long userId) throws EntityNotFoundException {
        Quest findQuest = questRepository.findByIdAndUserId(questId, userId);
        if (findQuest == null) {
            String entityNotFoundMessage = messageSourceAccessor.getMessage("exception.entity.notfound", new Object[]{messageSourceAccessor.getMessage("quest")});
            throw new EntityNotFoundException(entityNotFoundMessage);
        }
        return findQuest;
    }

    public QuestResponse deleteQuest(Long questId, Long userId) {
        Quest quest = this.getEntityOfUser(questId, userId);
        quest.deleteQuest();
        return QuestResponse.createDto(quest);
    }

    public QuestResponse completeQuest(Long userId, Long questId) {
        Quest quest = this.getEntityOfUser(questId, userId);
        quest.completeQuestIfPossible();
        switch (quest.getState()) {
            case COMPLETE -> {
                quest = questRepository.saveAndFlush(quest);
                QuestLogRequest questLogRequest = QuestLogRequest.from(quest);
                questLogService.saveQuestLog(questLogRequest);
                QuestCompletionUserUpdateRequest questCompletionUserUpdateRequest
                        = new QuestCompletionUserUpdateRequest(redisService.getQuestClearExp(), redisService.getQuestClearGold(), quest.getType());
                userService.addUserExpAndGold(userId, questCompletionUserUpdateRequest);
                userRecordService.recordGoldEarn(userId, questCompletionUserUpdateRequest);
                userRecordService.recordQuestCompletion(userId, questLogRequest.getLoggedDate());
            }
            case DELETE -> throw new IllegalStateException(messageSourceAccessor.getMessage("quest.error.deleted"));
            case PROCEED -> throw new IllegalStateException(messageSourceAccessor.getMessage("quest.error.complete.detail"));
            default -> throw new IllegalStateException(messageSourceAccessor.getMessage("quest.error.not-proceed"));
        }
        return QuestResponse.createDto(quest);
    }

    public QuestResponse discardQuest(Long questId, Long userId) {
        Quest quest = this.getEntityOfUser(questId, userId);
        quest.discardQuestIfPossible();
        switch (quest.getState()) {
            case DISCARD -> {
                quest = questRepository.saveAndFlush(quest);
                QuestLogRequest questLogRequest = QuestLogRequest.from(quest);
                questLogService.saveQuestLog(questLogRequest);
            }
            case DELETE -> throw new IllegalStateException(messageSourceAccessor.getMessage("quest.error.deleted"));
            default -> throw new IllegalStateException(messageSourceAccessor.getMessage("quest.error.not-proceed"));
        }
        return QuestResponse.createDto(quest);
    }

    public DetailResponse updateDetailQuestCount(Long userId, DetailInteractRequest request) {
        Quest quest = this.getProceedEntityOfUser(request.getQuestId(), userId);
        DetailQuest interactResult = quest.updateDetailQuestCount(request.getDetailQuestId(), request.getCount());
        if (interactResult == null) {
            throw new IllegalArgumentException(messageSourceAccessor.getMessage("exception.badRequest"));
        }
        return DetailResponse.of(interactResult, quest.canComplete());
    }
}
