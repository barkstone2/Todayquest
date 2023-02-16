package todayquest.quest.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import todayquest.common.MessageUtil;
import todayquest.quest.dto.QuestRequest;
import todayquest.quest.dto.QuestResponse;
import todayquest.quest.entity.DetailQuest;
import todayquest.quest.entity.Quest;
import todayquest.quest.entity.QuestState;
import todayquest.quest.repository.DetailQuestRepository;
import todayquest.quest.repository.QuestRepository;
import todayquest.user.dto.UserPrincipal;
import todayquest.user.entity.UserInfo;
import todayquest.user.repository.UserRepository;
import todayquest.user.service.UserService;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Transactional
@RequiredArgsConstructor
@Service
public class QuestService {

    private final QuestRepository questRepository;
    private final UserRepository userRepository;
    private final DetailQuestRepository detailQuestRepository;

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
        Quest savedQuest = questRepository.save(dto.mapToEntity(nextSeq, findUser));

        List<DetailQuest> detailQuests = dto.getDetails()
                .stream()
                .map(dq -> dq.mapToEntity(savedQuest))
                .collect(Collectors.toList());

        detailQuestRepository.saveAll(detailQuests);
        questLogService.saveQuestLog(savedQuest);

        return QuestResponse.createDto(savedQuest);
    }

    public QuestResponse updateQuest(QuestRequest dto, Long questId, Long userId) {
        Quest quest = findQuestIfNullThrow(questId);
        quest.checkIsQuestOfValidUser(userId);
        quest.checkIsProceedingQuest();

        if(quest.isMainQuest()) dto.toMainQuest();

        List<DetailQuest> newDetailQuests = quest.updateDetailQuests(dto.getDetails());

        quest.updateQuestEntity(dto);

        detailQuestRepository.saveAll(newDetailQuests);

        return QuestResponse.createDto(quest);
    }

    public void deleteQuest(Long questId, Long userId) {
        Quest quest = findQuestIfNullThrow(questId);
        quest.checkIsQuestOfValidUser(userId);
        quest.deleteQuest();
    }

    public void completeQuest(Long questId, UserPrincipal principal) throws IOException {
        Quest quest = findQuestIfNullThrow(questId);
        UserInfo questOwner = quest.getUser();
        quest.checkIsQuestOfValidUser(principal.getUserId());
        quest.completeQuest();

        userService.earnExpAndGold(quest.getType() ,questOwner, principal);
        questLogService.saveQuestLog(quest);
    }

    public void discardQuest(Long questId, Long userId) {
        Quest quest = findQuestIfNullThrow(questId);
        quest.checkIsQuestOfValidUser(userId);

        quest.discardQuest();

        questLogService.saveQuestLog(quest);
    }

    private Quest findQuestIfNullThrow(Long questId) {
        Optional<Quest> findQuest = questRepository.findById(questId);
        return findQuest.orElseThrow(() -> new IllegalArgumentException(
                MessageUtil.getMessage("exception.entity.notfound", MessageUtil.getMessage("quest"))));
    }
}
