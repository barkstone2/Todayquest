package todayquest.quest.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import todayquest.common.MessageUtil;
import todayquest.item.entity.Item;
import todayquest.item.entity.ItemLog;
import todayquest.item.entity.ItemLogType;
import todayquest.item.repository.ItemLogRepository;
import todayquest.item.repository.ItemRepository;
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

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Transactional
@RequiredArgsConstructor
@Service
public class QuestService {

    private final QuestRepository questRepository;
    private final UserRepository userRepository;
    private final RewardRepository rewardRepository;
    private final ItemRepository itemRepository;

    private final QuestRewardRepository questRewardRepository;

    private final ItemLogRepository itemLogRepository;
    private final ResourceLoader resourceLoader;

    public Slice<QuestResponseDto> getQuestList(Long userId, QuestState state, Pageable pageable) {

        PageRequest pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
        return questRepository.getQuestsList(userId, state, pageRequest)
                .map(QuestResponseDto::createDto);
    }

    public QuestResponseDto getQuestInfo(Long questId) {
        return QuestResponseDto.createDto(questRepository.getById(questId));
    }

    public Long saveQuest(QuestRequestDto dto, Long userId) {
        UserInfo findUser = userRepository.getById(userId);

        Long nextSeq = questRepository.getNextSeqByUserId(userId);
        Quest savedQuest = questRepository.save(dto.mapToEntity(nextSeq, findUser));

        List<Reward> rewards = rewardRepository.findAllByIdAndUserId(dto.getRewards(), userId);
        List<QuestReward> collect = rewards.stream()
                .map(r -> QuestReward.builder().reward(r).quest(savedQuest).build())
                .collect(Collectors.toList());
        questRewardRepository.saveAll(collect);

        return savedQuest.getId();
    }

    public void updateQuest(QuestRequestDto dto, Long questId, Long userId) {
        Optional<Quest> findQuest = questRepository.findById(questId);
        Quest quest = findQuest.orElseThrow(() -> new IllegalArgumentException(
                MessageUtil.getMessage("exception.entity.notfound", MessageUtil.getMessage("quest"))));

        if (quest.getUser().getId().equals(userId)) {
            if (!quest.getState().equals(QuestState.PROCEED)) {
                throw new IllegalArgumentException(MessageUtil.getMessage("quest.error.update.invalid.state"));
            }

            List<Reward> updateRewards = rewardRepository.findAllById(dto.getRewards());
            List<QuestReward> newRewards = quest.updateQuestEntity(dto, updateRewards);

            questRewardRepository.saveAll(newRewards);

        } else {
            throw new AccessDeniedException(MessageUtil.getMessage("exception.access.denied", MessageUtil.getMessage("quest")));
        }
    }

    public void deleteQuest(Long questId, Long userId) {
        Optional<Quest> findQuest = questRepository.findById(questId);
        Quest quest = findQuest.orElseThrow(() -> new IllegalArgumentException(
                MessageUtil.getMessage("exception.entity.notfound", MessageUtil.getMessage("quest"))));

        if (quest.getUser().getId().equals(userId)) {
            quest.changeState(QuestState.DELETE);
        } else {
            throw new AccessDeniedException(MessageUtil.getMessage("exception.access.denied", MessageUtil.getMessage("quest")));
        }
    }

    public void completeQuest(Long questId, UserPrincipal principal) throws IOException {
        // 경험치 테이블을 읽어온다.
        Resource resource = resourceLoader.getResource("classpath:data/exp_table.json");
        ObjectMapper om = new ObjectMapper();
        Map<Integer, Long> expTable = om.readValue(resource.getInputStream(), new TypeReference<>() {});
        Long targetExp = expTable.get(principal.getLevel());

        Optional<Quest> findQuest = questRepository.findById(questId);
        Quest quest = findQuest
                .orElseThrow(() -> new IllegalArgumentException(
                        MessageUtil.getMessage("exception.entity.notfound", MessageUtil.getMessage("quest")))
                );

        UserInfo questOwner = quest.getUser();
        if (questOwner.getId().equals(principal.getUserId())) {
            if(quest.getState().equals(QuestState.DELETE)) {
                throw new IllegalArgumentException(MessageUtil.getMessage("quest.error.deleted"));
            }

            // 퀘스트에 등록된 Reward 정보를 가져온다.
            List<Reward> rewardList = quest.getRewards().stream()
                    .map(qr -> qr.getReward())
                    .collect(Collectors.toList());

            // Reward의 ID 값만 가져온다.
            List<Long> rewardIds = rewardList.stream()
                    .map(r -> r.getId())
                    .collect(Collectors.toList());

            // 퀘스트 보상 아이템 중 이미 인벤토리에 있는 아이템을 찾아 dirty checking으로 개수를 증가시킨다.
            List<Item> dirtyCheckItemList = itemRepository.findAllByRewardIdsAndUserId(rewardIds, principal.getUserId());
            dirtyCheckItemList.stream().forEach(r -> r.addCount());

            // 더티 체킹으로 업데이트한 아이템의 Reward ID 값만 가져온다.
            List<Long> dirtyCheckIds = dirtyCheckItemList.stream()
                    .map(i -> i.getReward().getId())
                    .collect(Collectors.toList());

            // 퀘스트의 보상 아이템 중 더티체킹으로 업데이트 하지 않은, 즉 새로 등록할 아이템을 걸러내 새로 등록한다.
            List<Item> saveList = rewardList.stream()
                    .filter(r -> !dirtyCheckIds.contains(r.getId()))
                    .map(r -> Item.builder().reward(r).user(questOwner).build())
                    .collect(Collectors.toList());
            itemRepository.saveAll(saveList);

            // 퀘스트의 상태를 완료 상태로 변경한다.
            quest.changeState(QuestState.COMPLETE);

            // 더티체킹으로 퀘스트를 클리어한 유저의 경험치와 골드를 증가시킨다.
            // 레벨업이 가능한 경우 레벨업을 시킨다.
            questOwner.earnExpAndGold(quest.getDifficulty().getExperience(), quest.getDifficulty().getGold());
            questOwner.levelUpCheck(targetExp);

            // 로그인된 사용자의 정보를 동기화한다.
            principal.synchronizeUserInfo(questOwner.getLevel(), questOwner.getExp(), questOwner.getGold());

            // 아이템 획득 로그 저장
            List<ItemLog> itemEarnLogs = rewardIds.stream()
                    .map(rewardId -> ItemLog.builder()
                            .rewardId(rewardId)
                            .userId(principal.getUserId())
                            .type(ItemLogType.EARN)
                            .build()
                    )
                    .collect(Collectors.toList());
            itemLogRepository.saveAll(itemEarnLogs);
        } else {
            throw new AccessDeniedException(MessageUtil.getMessage("exception.access.denied", MessageUtil.getMessage("quest")));
        }
    }

    public void discardQuest(Long questId, Long userId) {
        Optional<Quest> findQuest = questRepository.findById(questId);
        Quest quest = findQuest.orElseThrow(() -> new IllegalArgumentException(
                MessageUtil.getMessage("exception.entity.notfound", MessageUtil.getMessage("quest"))));

        if (quest.getUser().getId().equals(userId)) {
            if(quest.getState().equals(QuestState.DELETE)) {
                throw new IllegalArgumentException(MessageUtil.getMessage("quest.error.deleted"));
            }

            quest.changeState(QuestState.DISCARD);
        } else {
            throw new AccessDeniedException(MessageUtil.getMessage("exception.access.denied", MessageUtil.getMessage("quest")));
        }
    }
}
