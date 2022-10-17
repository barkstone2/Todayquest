package todayquest.reward.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import todayquest.reward.dto.RewardRequestDto;
import todayquest.reward.dto.RewardResponseDto;
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
public class RewardService {
    private final RewardRepository rewardRepository;
    private final UserRepository userRepository;

    public List<RewardResponseDto> getRewardList(Long userId) {
        return rewardRepository.findAllByUserId(userId).stream()
                .map(RewardResponseDto::createDto)
                .collect(Collectors.toList());
    }

    public RewardResponseDto getReward(Long id, Long userId) {
        return RewardResponseDto.createDto(rewardRepository.findByIdAndUserId(id, userId));
    }

    public void saveReward(RewardRequestDto dto, Long userId) {
        UserInfo findUser = userRepository.getById(userId);
        rewardRepository.save(dto.mapToEntity(findUser));
    }

    public void updateReward(RewardRequestDto dto, Long rewardId, Long userId) {
        Reward findReward = rewardRepository.findByIdAndUserId(rewardId, userId);
        findReward.updateReward(dto);
    }

    public void deleteReward(Long rewardId, Long userId) {
        rewardRepository.deleteByIdAndUserId(rewardId, userId);
    }


}
