package todayquest.quest.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import todayquest.common.MessageUtil;
import todayquest.item.service.ItemService;
import todayquest.quest.dto.QuestRequestDto;
import todayquest.quest.dto.QuestResponseDto;
import todayquest.quest.entity.Quest;
import todayquest.quest.entity.QuestReward;
import todayquest.quest.entity.QuestState;
import todayquest.quest.repository.QuestRepository;
import todayquest.quest.repository.QuestRewardRepository;
import todayquest.reward.entity.Reward;
import todayquest.reward.repository.RewardRepository;
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
    private final RewardRepository rewardRepository;
    private final QuestRewardRepository questRewardRepository;

    private final ItemService itemService;
    private final UserService userService;
    private final QuestLogService questLogService;


    public Slice<QuestResponseDto> getQuestList(Long userId, QuestState state, Pageable pageable) {

        PageRequest pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
        return questRepository.getQuestsList(userId, state, pageRequest)
                .map(QuestResponseDto::createDto);
    }

    public QuestResponseDto getQuestInfo(Long questId, Long userId) {
        Quest quest = findQuestWithValidation(questId);
        checkQuestOwner(quest.getUser().getId(), userId);

        return QuestResponseDto.createDto(quest);
    }

    public void saveQuest(QuestRequestDto dto, Long userId) {
        UserInfo findUser = userRepository.getById(userId);

        Long nextSeq = questRepository.getNextSeqByUserId(userId);
        Quest savedQuest = questRepository.save(dto.mapToEntity(nextSeq, findUser));

        List<Reward> rewards = rewardRepository.findAllByIdAndUserId(dto.getRewards(), userId);
        List<QuestReward> collect = rewards.stream()
                .map(r -> QuestReward.builder().reward(r).quest(savedQuest).build())
                .collect(Collectors.toList());
        questRewardRepository.saveAll(collect);
    }

    public void updateQuest(QuestRequestDto dto, Long questId, Long userId) {
        Quest quest = findQuestWithValidation(questId);
        checkQuestOwner(quest.getUser().getId(), userId);

        if (!quest.getState().equals(QuestState.PROCEED)) {
            throw new IllegalArgumentException(MessageUtil.getMessage("quest.error.update.invalid.state"));
        }

        List<Reward> updateRewards = rewardRepository.findAllById(dto.getRewards());
        List<QuestReward> newRewards = quest.updateQuestEntity(dto, updateRewards);
        questRewardRepository.saveAll(newRewards);
    }



    public void deleteQuest(Long questId, Long userId) {
        Quest quest = findQuestWithValidation(questId);
        checkQuestOwner(quest.getUser().getId(), userId);
        quest.changeState(QuestState.DELETE);
    }

    public void completeQuest(Long questId, UserPrincipal principal) throws IOException {
        Quest quest = findQuestWithValidation(questId);
        UserInfo questOwner = quest.getUser();
        checkQuestOwner(quest.getUser().getId(), principal.getUserId());

        if(quest.getState().equals(QuestState.DELETE)) {
            throw new IllegalArgumentException(MessageUtil.getMessage("quest.error.deleted"));
        }

        // 퀘스트의 상태를 완료 상태로 변경한다.
        quest.changeState(QuestState.COMPLETE);

        // 퀘스트에 등록된 Reward 정보를 가져온다.
        List<Reward> rewardList = quest.getRewards().stream()
                .map(qr -> qr.getReward())
                .collect(Collectors.toList());

        // 아이템 획득 처리
        itemService.saveAllWithDirtyChecking(rewardList, questOwner);

        // 경험치 골드 획득 처리
        userService.earnExpAndGold(questOwner, quest.getDifficulty(), principal);

        // 퀘스트 완료 로그 저장
        questLogService.saveQuestLog(questId, principal.getUserId(), QuestState.COMPLETE);
    }

    public void discardQuest(Long questId, Long userId) {
        Quest quest = findQuestWithValidation(questId);
        checkQuestOwner(quest.getUser().getId(), userId);

        if(quest.getState().equals(QuestState.DELETE)) {
            throw new IllegalArgumentException(MessageUtil.getMessage("quest.error.deleted"));
        }

        quest.changeState(QuestState.DISCARD);
        questLogService.saveQuestLog(questId, userId, QuestState.DISCARD);
    }

    private void checkQuestOwner(Long ownerUserId, Long userId) {
        if (!ownerUserId.equals(userId)) throw new AccessDeniedException(MessageUtil.getMessage("exception.access.denied", MessageUtil.getMessage("quest")));
    }

    private Quest findQuestWithValidation(Long questId) {
        Optional<Quest> findQuest = questRepository.findById(questId);
        Quest quest = findQuest.orElseThrow(() -> new IllegalArgumentException(
                MessageUtil.getMessage("exception.entity.notfound", MessageUtil.getMessage("quest"))));
        return quest;
    }
}
