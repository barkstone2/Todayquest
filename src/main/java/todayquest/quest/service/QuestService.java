package todayquest.quest.service;

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
import todayquest.user.entity.UserInfo;
import todayquest.user.repository.UserRepository;

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
    private final ItemRepository itemRepository;

    private final QuestRewardRepository questRewardRepository;

    @Transactional
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
            quest.updateQuestEntity(dto, updateRewards);

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

    public void completeQuest(Long questId, Long userId) {
        Optional<Quest> findQuest = questRepository.findById(questId);
        Quest quest = findQuest
                .orElseThrow(() -> new IllegalArgumentException(
                        MessageUtil.getMessage("exception.entity.notfound", MessageUtil.getMessage("quest")))
                );

        UserInfo questOwner = quest.getUser();
        if (questOwner.getId().equals(userId)) {
            if(quest.getState().equals(QuestState.DELETE)) {
                throw new IllegalArgumentException(MessageUtil.getMessage("quest.error.deleted"));
            }

            quest.changeState(QuestState.COMPLETE);

            List<Reward> rewardList = quest.getRewards().stream()
                    .map(qr -> qr.getReward()).collect(Collectors.toList());

            List<Long> dirtyCheckIds = rewardList.stream()
                    .map(r -> r.getId())
                    .collect(Collectors.toList());
            List<Item> dirtyCheckList = itemRepository.findAllByRewardIdsAndUserId(dirtyCheckIds, userId);
            dirtyCheckList.stream().forEach(r -> r.addCount());

            List<Item> saveList = rewardList.stream().filter(r -> !dirtyCheckIds.contains(r.getId()))
                    .map(r -> Item.builder().reward(r).user(questOwner).build())
                    .collect(Collectors.toList());

            itemRepository.saveAll(saveList);
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
