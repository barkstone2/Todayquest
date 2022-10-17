package todayquest.quest.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import todayquest.quest.dto.QuestRequestDto;
import todayquest.quest.dto.QuestResponseDto;
import todayquest.quest.entity.Quest;
import todayquest.quest.entity.QuestReward;
import todayquest.quest.repository.QuestRepository;
import todayquest.quest.repository.QuestRewardRepository;
import todayquest.reward.entity.Reward;
import todayquest.reward.repository.RewardRepository;
import todayquest.user.dto.UserPrincipal;
import todayquest.user.entity.UserInfo;
import todayquest.user.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Transactional
@RequiredArgsConstructor
@Service
public class QuestService {

    private final QuestRepository questRepository;
    private final UserRepository userRepository;
    private final RewardRepository rewardRepository;
    private final QuestRewardRepository questRewardRepository;


    public List<QuestResponseDto> getQuestList(Long userId) {
        return questRepository.getQuestsByUserOrderByDeadLineDateAscDeadLineTimeAsc(UserInfo.builder().id(userId).build())
                .stream()
                .map(QuestResponseDto::createDto)
                .collect(Collectors.toList());
    }

    public QuestResponseDto getQuestInfo(Long questId) {
        return QuestResponseDto.createDto(questRepository.getById(questId));
    }

    public void saveQuest(QuestRequestDto dto, UserPrincipal principal) {
        UserInfo findId = userRepository.getById(principal.getUserId());
        Quest savedQuest = questRepository.save(dto.mapToEntity(findId));

        List<Reward> rewards = rewardRepository.findAllById(dto.getRewards());
        List<QuestReward> collect = rewards.stream()
                .map(r -> QuestReward.builder().reward(r).quest(savedQuest).build())
                .collect(Collectors.toList());
        questRewardRepository.saveAll(collect);
    }

    public void updateQuest(QuestRequestDto dto, Long questId) {
        Quest findQuest = questRepository.getById(questId);
        List<Reward> updateRewards = rewardRepository.findAllById(dto.getRewards());

        findQuest.updateQuestEntity(dto, updateRewards);
    }

    public void deleteQuest(Long questId) {
        questRepository.deleteById(questId);
    }
}
