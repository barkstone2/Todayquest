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

    public List<RewardResponseDto> getRewardList(UserPrincipal principal) {
        return rewardRepository.findByUserId(principal.getUserId()).stream()
                .map(RewardResponseDto::createDto)
                .collect(Collectors.toList());
    }

    public RewardResponseDto getReward(Long id, UserPrincipal principal) {
        return RewardResponseDto.createDto(rewardRepository.findByIdAndUserId(id, principal.getUserId()));
    }

    public void saveReward(RewardRequestDto dto, UserPrincipal principal) {
        UserInfo findUser = userRepository.getById(principal.getUserId());
        rewardRepository.save(dto.mapToEntity(findUser));
    }

    public void updateReward(RewardRequestDto dto, Long rewardId, UserPrincipal principal) {
        Reward findReward = rewardRepository.findByIdAndUserId(rewardId, principal.getUserId());
        findReward.updateReward(dto);
    }

    public void deleteReward(Long rewardId, UserPrincipal principal) {
        rewardRepository.deleteByIdAndUserId(rewardId, principal.getUserId());
    }


}
