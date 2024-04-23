package dailyquest.quest.service;

import dailyquest.quest.dto.*;
import dailyquest.quest.entity.DetailQuest;
import dailyquest.quest.entity.Quest;
import dailyquest.quest.entity.QuestState;
import dailyquest.quest.repository.QuestRepository;
import dailyquest.user.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
@Service
public class QuestCommandService {
    private final QuestRepository questRepository;
    private final UserService userService;
    private final QuestLogService questLogService;
    private final MessageSourceAccessor messageSourceAccessor;

    @Autowired
    public QuestCommandService(QuestRepository questRepository, UserService userService, QuestLogService questLogService, MessageSource messageSource) {
        this.questRepository = questRepository;
        this.userService = userService;
        this.questLogService = questLogService;
        this.messageSourceAccessor = new MessageSourceAccessor(messageSource);
    }

    public QuestResponse saveQuest(QuestRequest dto, Long userId) {
        Long nextSeq = questRepository.getNextSeqOfUser(userId);
        Quest quest = dto.mapToEntity(nextSeq, userId);
        questRepository.save(quest);
        QuestLogRequest questLogRequest = QuestLogRequest.from(quest);
        questLogService.saveQuestLog(questLogRequest);
        userService.recordQuestRegistration(userId, questLogRequest.getLoggedDate());
        return QuestResponse.createDto(quest);
    }

    public QuestResponse updateQuest(QuestRequest dto, Long questId, Long userId) {
        Quest quest = this.getProceedEntityOfUser(questId, userId);
        List<DetailQuest> details = dto.getDetails().stream().map(detail -> detail.mapToEntity(quest)).toList();
        quest.updateQuestEntity(dto.getTitle(), dto.getDescription(), dto.getDeadLine(), details);
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

    public QuestResponse completeQuest(Long userId, QuestCompletionRequest questCompletionRequest) {
        Quest quest = this.getEntityOfUser(questCompletionRequest.getQuestId(), userId);
        QuestState resultState = quest.completeQuest();
        switch (resultState) {
            case COMPLETE -> {
                if (quest.isMainQuest()) {
                    questCompletionRequest.toMainQuest();
                }
                userService.addUserExpAndGold(userId, questCompletionRequest);
                QuestLogRequest questLogRequest = QuestLogRequest.from(quest);
                questLogService.saveQuestLog(questLogRequest);
                userService.recordQuestCompletion(userId, questLogRequest.getLoggedDate());
            }
            case DELETE -> throw new IllegalStateException(messageSourceAccessor.getMessage("quest.error.deleted"));
            case PROCEED -> throw new IllegalStateException(messageSourceAccessor.getMessage("quest.error.complete.detail"));
            default -> throw new IllegalStateException(messageSourceAccessor.getMessage("quest.error.not-proceed"));
        }
        return QuestResponse.createDto(quest);
    }

    public QuestResponse discardQuest(Long questId, Long userId) {
        Quest quest = this.getEntityOfUser(questId, userId);
        QuestState resultState = quest.discardQuest();
        switch (resultState) {
            case DISCARD -> {
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
        return DetailResponse.of(interactResult, quest.canComplete());
    }
}
