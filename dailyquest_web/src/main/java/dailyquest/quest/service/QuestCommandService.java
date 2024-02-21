package dailyquest.quest.service;

import dailyquest.common.MessageUtil;
import dailyquest.quest.dto.DetailInteractRequest;
import dailyquest.quest.dto.DetailResponse;
import dailyquest.quest.dto.QuestRequest;
import dailyquest.quest.dto.QuestResponse;
import dailyquest.quest.entity.DetailQuest;
import dailyquest.quest.entity.Quest;
import dailyquest.quest.entity.QuestState;
import dailyquest.quest.repository.QuestRepository;
import dailyquest.user.entity.UserInfo;
import dailyquest.user.repository.UserRepository;
import dailyquest.user.service.UserService;
import jakarta.annotation.Nullable;
import kotlin.Pair;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
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
        UserInfo findUser = userRepository.getReferenceById(userId);
        dto.checkRangeOfDeadLine();

        if (findUser.isNowCoreTime()) {
            dto.toMainQuest();
        }

        Long nextSeq = questRepository.getNextSeqByUserId(userId);

        Quest quest = dto.mapToEntity(nextSeq, findUser);
        questRepository.save(quest);

        List<Pair<Long, DetailQuest>> details = dto.getDetails().stream()
                .map(detail -> new Pair<>(detail.getId(), detail.mapToEntity(quest)))
                .toList();

        quest.updateDetailQuests(details);

        questLogService.saveQuestLog(quest);

        return QuestResponse.createDto(quest);
    }

    public QuestResponse updateQuest(QuestRequest dto, Long questId, Long userId) {
        Quest quest = questQueryService.findByIdOrThrow(questId);
        dto.checkRangeOfDeadLine();
        if(!quest.isQuestOfUser(userId)) throw new AccessDeniedException(MessageUtil.getMessage("exception.access.denied"));
        if(!quest.isProceed()) throw new IllegalStateException(MessageUtil.getMessage("quest.error.not-proceed"));

        if(quest.isMainQuest()) dto.toMainQuest();

        List<Pair<Long, DetailQuest>> details = dto.getDetails().stream()
                .map(detail -> new Pair<>(detail.getId(), detail.mapToEntity(quest)))
                .toList();

        quest.updateQuestEntity(dto.getTitle(), dto.getDescription(), dto.getDeadLine(), details);

        return QuestResponse.createDto(quest);
    }

    public QuestResponse deleteQuest(Long questId, Long userId) {
        Quest quest = questQueryService.findByIdOrThrow(questId);
        if(!quest.isQuestOfUser(userId)) throw new AccessDeniedException(MessageUtil.getMessage("exception.access.denied"));

        quest.deleteQuest();
        return QuestResponse.createDto(quest);
    }

    public QuestResponse completeQuest(Long questId, Long userId) {
        Quest quest = questQueryService.findByIdOrThrow(questId);
        if(!quest.isQuestOfUser(userId)) throw new AccessDeniedException(MessageUtil.getMessage("exception.access.denied"));

        QuestState resultState = quest.completeQuest();

        switch (resultState) {
            case COMPLETE -> {
                userService.earnExpAndGold(quest.getType(), quest.getUser());
                questLogService.saveQuestLog(quest);
            }
            case DELETE -> throw new IllegalStateException(MessageUtil.getMessage("quest.error.deleted"));
            case PROCEED -> throw new IllegalStateException(MessageUtil.getMessage("quest.error.complete.detail"));
            default -> throw new IllegalStateException(MessageUtil.getMessage("quest.error.not-proceed"));
        }

        return QuestResponse.createDto(quest);
    }

    public QuestResponse discardQuest(Long questId, Long userId) {
        Quest quest = questQueryService.findByIdOrThrow(questId);
        if(!quest.isQuestOfUser(userId)) throw new AccessDeniedException(MessageUtil.getMessage("exception.access.denied"));

        QuestState resultState = quest.discardQuest();
        switch (resultState) {
            case DISCARD -> {
                questLogService.saveQuestLog(quest);
            }
            case DELETE -> throw new IllegalStateException(MessageUtil.getMessage("quest.error.deleted"));
            default -> throw new IllegalStateException(MessageUtil.getMessage("quest.error.not-proceed"));
        }
        return QuestResponse.createDto(quest);
    }

    public DetailResponse interactWithDetailQuest(Long userId, Long questId, Long detailQuestId, @Nullable DetailInteractRequest request) {
        Quest quest = questQueryService.findByIdOrThrow(questId);
        if(!quest.isQuestOfUser(userId)) throw new AccessDeniedException(MessageUtil.getMessage("exception.access.denied"));
        if(!quest.isProceed()) throw new IllegalStateException(MessageUtil.getMessage("quest.error.not-proceed"));

        DetailQuest interactResult = quest.interactWithDetailQuest(detailQuestId, request == null ? null : request.getCount());
        if(interactResult == null) throw new IllegalStateException(MessageUtil.getMessage("exception.badRequest"));

        return DetailResponse.Companion.createDto(interactResult, quest.canComplete());
    }
}
