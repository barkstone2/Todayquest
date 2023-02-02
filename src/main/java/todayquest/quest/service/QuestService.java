package todayquest.quest.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import todayquest.achievement.service.AchievementService;
import todayquest.common.MessageUtil;
import todayquest.item.service.ItemService;
import todayquest.quest.dto.QuestRequestDto;
import todayquest.quest.dto.QuestResponseDto;
import todayquest.quest.entity.*;
import todayquest.quest.repository.DetailQuestRepository;
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
    private final DetailQuestRepository detailQuestRepository;

    private final ItemService itemService;
    private final UserService userService;
    private final QuestLogService questLogService;
    private final AchievementService achievementService;


    public Slice<QuestResponseDto> getQuestList(Long userId, QuestState state, Pageable pageable) {

        PageRequest pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
        return questRepository.getQuestsList(userId, state, pageRequest)
                .map(QuestResponseDto::createDto);
    }

    public QuestResponseDto getQuestInfo(Long questId, Long userId) {
        Quest quest = findQuestIfNullThrow(questId);
        quest.checkIsQuestOfValidUser(userId);

        return QuestResponseDto.createDto(quest);
    }

    public void saveQuest(QuestRequestDto dto, Long userId) {
        UserInfo findUser = userRepository.getById(userId);

        if (findUser.isNowCoreTime()) {
            dto.setType(QuestType.MAIN);
        }

        Long nextSeq = questRepository.getNextSeqByUserId(userId);
        Quest savedQuest = questRepository.save(dto.mapToEntity(nextSeq, findUser));

        List<Reward> rewards = rewardRepository.findAllByIdAndUserId(dto.getRewards(), userId);
        List<QuestReward> collect = rewards.stream()
                .map(r -> new QuestReward(savedQuest, r))
                .collect(Collectors.toList());
        questRewardRepository.saveAll(collect);

        List<DetailQuest> detailQuests = dto.getDetailQuests()
                .stream()
                .map(dq -> dq.mapToEntity(savedQuest))
                .collect(Collectors.toList());

        detailQuestRepository.saveAll(detailQuests);

        questLogService.saveQuestLog(savedQuest);

        achievementService.checkAndAttainQuestAchievement(userId);
    }

    public void updateQuest(QuestRequestDto dto, Long questId, Long userId) {
        Quest quest = findQuestIfNullThrow(questId);
        quest.checkIsQuestOfValidUser(userId);
        quest.checkIsProceedingQuest();

        List<Reward> updateRewards = rewardRepository.findAllById(dto.getRewards());

        List<QuestReward> newRewards = quest.updateQuestEntity(dto, updateRewards);
        List<DetailQuest> newDetailQuests = quest.updateDetailQuests(dto.getDetailQuests());

        questRewardRepository.saveAll(newRewards);
        detailQuestRepository.saveAll(newDetailQuests);
    }



    public void deleteQuest(Long questId, Long userId) {
        Quest quest = findQuestIfNullThrow(questId);
        quest.checkIsQuestOfValidUser(userId);
        quest.deleteQuest();
        achievementService.checkAndAttainQuestAchievement(userId);
    }

    public void completeQuest(Long questId, UserPrincipal principal) throws IOException {
        Quest quest = findQuestIfNullThrow(questId);
        UserInfo questOwner = quest.getUser();
        quest.checkIsQuestOfValidUser(principal.getUserId());
        quest.completeQuest();

        /**
         * 퀘스트에 등록된 Reward 정보를 가져온다.
         * QuestReward 테이블을 1+1 조회한 후 Reward ID에 대해서는 조회 쿼리를 날리지 않는다.
         * -> QuestReward 조회시 Reward ID를 같이 가져 온다.
         */
        List<Reward> rewardList = quest.getRewards().stream()
                .map(QuestReward::getReward)
                .collect(Collectors.toList());

        // 아이템 획득 처리
        itemService.saveAllWithDirtyChecking(rewardList, questOwner);

        // 경험치 골드 획득 처리
        userService.earnExpAndGold(quest.getType() ,questOwner, principal);

        // 퀘스트 완료 로그 저장
        questLogService.saveQuestLog(quest);

        achievementService.checkAndAttainQuestAchievement(principal.getUserId());
    }

    public void discardQuest(Long questId, Long userId) {
        Quest quest = findQuestIfNullThrow(questId);
        quest.checkIsQuestOfValidUser(userId);

        quest.discardQuest();

        questLogService.saveQuestLog(quest);
        achievementService.checkAndAttainQuestAchievement(userId);
    }

    private Quest findQuestIfNullThrow(Long questId) {
        Optional<Quest> findQuest = questRepository.findById(questId);
        return findQuest.orElseThrow(() -> new IllegalArgumentException(
                MessageUtil.getMessage("exception.entity.notfound", MessageUtil.getMessage("quest"))));
    }
}
