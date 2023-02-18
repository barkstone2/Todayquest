package todayquest.quest.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import todayquest.common.MessageUtil;
import todayquest.common.RestPage;
import todayquest.quest.dto.DetailInteractRequest;
import todayquest.quest.dto.DetailResponse;
import todayquest.quest.dto.QuestRequest;
import todayquest.quest.dto.QuestResponse;
import todayquest.quest.entity.Quest;
import todayquest.quest.entity.QuestState;
import todayquest.quest.repository.QuestRepository;
import todayquest.user.entity.UserInfo;
import todayquest.user.repository.UserRepository;
import todayquest.user.service.UserService;

import java.util.Optional;

@Transactional
@RequiredArgsConstructor
@Service
public class QuestService {

    private final QuestRepository questRepository;
    private final UserRepository userRepository;

    private final UserService userService;
    private final QuestLogService questLogService;

    public RestPage<QuestResponse> getQuestList(Long userId, QuestState state, Pageable pageable) {

        PageRequest pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());

        return new RestPage<>(
                questRepository
                        .getQuestsList(userId, state, pageRequest)
                        .map(QuestResponse::createDto)
        );
    }

    public QuestResponse getQuestInfo(Long questId, Long userId) {
        Quest quest = findQuestIfNullThrow(questId);
        quest.checkIsQuestOfValidUser(userId);

        return QuestResponse.createDto(quest);
    }

    public QuestResponse saveQuest(QuestRequest dto, Long userId) {
        UserInfo findUser = userRepository.getReferenceById(userId);

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
        Quest quest = findQuestIfNullThrow(questId);
        quest.checkIsQuestOfValidUser(userId);
        quest.checkIsProceedingQuest();

        if(quest.isMainQuest()) dto.toMainQuest();

        quest.updateQuestEntity(dto);

        return QuestResponse.createDto(quest);
    }

    public void deleteQuest(Long questId, Long userId) {
        Quest quest = findQuestIfNullThrow(questId);
        quest.checkIsQuestOfValidUser(userId);
        quest.deleteQuest();
    }

    public void completeQuest(Long questId, Long userId) {
        Quest quest = findQuestIfNullThrow(questId);
        quest.checkIsQuestOfValidUser(userId);
        quest.completeQuest();

        userService.earnExpAndGold(quest.getType(), quest.getUser());
        questLogService.saveQuestLog(quest);
    }

    public void discardQuest(Long questId, Long userId) {
        Quest quest = findQuestIfNullThrow(questId);
        quest.checkIsQuestOfValidUser(userId);

        quest.discardQuest();

        questLogService.saveQuestLog(quest);
    }

    public DetailResponse interactWithDetailQuest(Long userId, Long questId, Long detailQuestId, DetailInteractRequest request) {
        Quest quest = findQuestIfNullThrow(questId);
        quest.checkIsQuestOfValidUser(userId);

        return quest.interactWithDetailQuest(detailQuestId, request);
    }

    private Quest findQuestIfNullThrow(Long questId) {
        Optional<Quest> findQuest = questRepository.findById(questId);
        return findQuest.orElseThrow(() -> new IllegalArgumentException(
                MessageUtil.getMessage("exception.entity.notfound", MessageUtil.getMessage("quest"))));
    }
}
