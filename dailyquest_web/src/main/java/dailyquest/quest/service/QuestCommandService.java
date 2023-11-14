package dailyquest.quest.service;

import dailyquest.quest.dto.DetailInteractRequest;
import dailyquest.quest.dto.DetailResponse;
import dailyquest.quest.dto.QuestRequest;
import dailyquest.quest.dto.QuestResponse;
import dailyquest.quest.entity.Quest;
import dailyquest.quest.repository.QuestRepository;
import dailyquest.user.entity.UserInfo;
import dailyquest.user.repository.UserRepository;
import dailyquest.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        dto.checkRangeOfDeadLine(findUser.getResetTime());

        if (findUser.isNowCoreTime()) {
            dto.toMainQuest();
        }

        Long nextSeq = questRepository.getNextSeqByUserId(userId);

        Quest quest = dto.mapToEntity(nextSeq, findUser);
        questRepository.save(quest);

        quest.updateDetailQuests(dto.getDetails());

        questLogService.saveQuestLog(quest);

        return QuestResponse.createDto(quest);
    }

    public QuestResponse updateQuest(QuestRequest dto, Long questId, Long userId) {
        Quest quest = questQueryService.findByIdOrThrow(questId);
        dto.checkRangeOfDeadLine(quest.getUser().getResetTime());
        quest.checkOwnershipOrThrow(userId);
        quest.checkStateIsProceedOrThrow();

        if(quest.isMainQuest()) dto.toMainQuest();

        quest.updateQuestEntity(dto);

        return QuestResponse.createDto(quest);
    }

    public void deleteQuest(Long questId, Long userId) {
        Quest quest = questQueryService.findByIdOrThrow(questId);
        quest.checkOwnershipOrThrow(userId);
        quest.deleteQuest();
    }

    public void completeQuest(Long questId, Long userId) {
        Quest quest = questQueryService.findByIdOrThrow(questId);
        quest.checkOwnershipOrThrow(userId);
        quest.completeQuest();

        userService.earnExpAndGold(quest.getType(), quest.getUser());
        questLogService.saveQuestLog(quest);
    }

    public void discardQuest(Long questId, Long userId) {
        Quest quest = questQueryService.findByIdOrThrow(questId);
        quest.checkOwnershipOrThrow(userId);

        quest.discardQuest();

        questLogService.saveQuestLog(quest);
    }

    public DetailResponse interactWithDetailQuest(Long userId, Long questId, Long detailQuestId, DetailInteractRequest request) {
        Quest quest = questQueryService.findByIdOrThrow(questId);
        quest.checkOwnershipOrThrow(userId);

        return quest.interactWithDetailQuest(detailQuestId, request);
    }
}
