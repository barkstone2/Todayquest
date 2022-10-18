package todayquest.reward.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import todayquest.common.MessageUtil;
import todayquest.reward.dto.RewardRequestDto;
import todayquest.reward.dto.RewardResponseDto;
import todayquest.reward.entity.Reward;
import todayquest.reward.repository.RewardRepository;
import todayquest.user.dto.UserPrincipal;
import todayquest.user.entity.UserInfo;
import todayquest.user.repository.UserRepository;

import java.util.List;
import java.util.Optional;
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
        Optional<Reward> findReward = rewardRepository.findByIdAndUserId(id, userId);
        return RewardResponseDto.createDto(
                findReward.orElseThrow(
                        () -> new IllegalArgumentException(MessageUtil.getMessage("exception.entity.notfound", MessageUtil.getMessage("item")))
                )
        );
    }

    public void saveReward(RewardRequestDto dto, Long userId) {
        Optional<UserInfo> findUser = userRepository.findById(userId);
        rewardRepository.save(dto.mapToEntity(findUser.orElseThrow(() -> new IllegalStateException(MessageUtil.getMessage("exception.login.expire")))));
    }

    public void updateReward(RewardRequestDto dto, Long rewardId, Long userId) {
        Optional<Reward> findReward = rewardRepository.findByIdAndUserId(rewardId, userId);
        findReward.orElseThrow(() -> new IllegalArgumentException(MessageUtil.getMessage("exception.entity.notfound", MessageUtil.getMessage("item")))).updateReward(dto);
    }

    public void deleteReward(Long rewardId, Long userId) {
        long deleteCount = rewardRepository.deleteByIdAndUserId(rewardId, userId);
        if(deleteCount == 0) throw new IllegalArgumentException(MessageUtil.getMessage("exception.entity.notfound", MessageUtil.getMessage("item")));
    }

    public List<RewardResponseDto> getRewardListByIds(List<Long> ids, Long userId) {
        return rewardRepository.findAllByIdAndUserId(ids, userId).stream().map(RewardResponseDto::createDto).collect(Collectors.toList());
    }

}
