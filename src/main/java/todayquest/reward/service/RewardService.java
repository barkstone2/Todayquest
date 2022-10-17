package todayquest.reward.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import todayquest.reward.dto.RewardRequestDto;
import todayquest.reward.dto.RewardResponseDto;
import todayquest.reward.repository.RewardRepository;
import todayquest.user.dto.UserPrincipal;
import todayquest.user.entity.UserInfo;
import todayquest.user.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class RewardService {
    private final RewardRepository rewardRepository;
    private final UserRepository userRepository;

    public List<RewardResponseDto> getRewardList(UserPrincipal principal) {
        UserInfo findUser = userRepository.getById(principal.getUserId());
        return rewardRepository.findByUser(findUser).stream().map(RewardResponseDto::createDto).collect(Collectors.toList());
    }
}
