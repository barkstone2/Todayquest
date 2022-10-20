package todayquest.reward.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.security.core.userdetails.User;
import todayquest.common.MessageUtil;
import todayquest.quest.service.QuestService;
import todayquest.reward.dto.RewardRequestDto;
import todayquest.reward.dto.RewardResponseDto;
import todayquest.reward.entity.Reward;
import todayquest.reward.repository.RewardRepository;
import todayquest.user.entity.UserInfo;
import todayquest.user.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.ReadWriteLock;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@DisplayName("보상 아이템 서비스 유닛 테스트")
@ExtendWith(MockitoExtension.class)
class RewardServiceTest {

    @InjectMocks
    RewardService rewardService;

    @Mock
    RewardRepository rewardRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    MessageSource messageSource;

    @InjectMocks
    MessageUtil messageUtil;

    @DisplayName("보상 아이템 목록 조회")
    @Test
    void getRewardList() {
        //given
        List<Reward> list = List.of(Reward.builder().name("name").build());

        //when
        when(rewardRepository.findAllByUserId(any())).thenReturn(list);
        List<RewardResponseDto> rewardList = rewardService.getRewardList(1L);

        //then
        assertThat(rewardList.size()).isEqualTo(1);
        assertThat(rewardList.get(0).getName()).isEqualTo(list.get(0).getName());
    }

    @DisplayName("보상 아이템 조회")
    @Test
    void getReward() {
        //given
        Reward reward = Reward.builder()
                .name("name")
                .user(UserInfo.builder().id(1L).build())
                .build();

        //when
        when(rewardRepository.findById(any())).thenReturn(Optional.ofNullable(reward));
        RewardResponseDto dto = rewardService.getReward(1L, 1L);

        //then
        assertThat(dto.getName()).isEqualTo(reward.getName());
    }

    @DisplayName("보상 아이템 조회 시 NULL 반환")
    @Test
    void getRewardFail() {
        //given
        Reward reward = Reward.builder()
                .name("name")
                .build();

        //when
        when(rewardRepository.findById(any())).thenReturn(Optional.ofNullable(null));

        //then
        assertThatThrownBy(() -> rewardService.getReward(1L, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(MessageUtil.getMessage("exception.entity.notfound", MessageUtil.getMessage("item")));
    }


    @DisplayName("보상 아이템 저장")
    @Test
    void saveReward() {
        //given
        RewardRequestDto dto = RewardRequestDto.builder().name("save name").build();

        //when
        //then
        when(rewardRepository.save(any())).thenReturn(Reward.builder().build());
        when(userRepository.getById(any())).thenReturn(UserInfo.builder().build());
        rewardService.saveReward(dto, 1L);

    }

    @DisplayName("보상 아이템 업데이트_성공")
    @Test
    void updateReward() {
        //given
        Reward reward = Reward.builder().name("save name").user(UserInfo.builder().id(1L).build()).build();

        //when
        when(rewardRepository.findById(any())).thenReturn(Optional.ofNullable(reward));
        rewardService.updateReward(RewardRequestDto.builder().name("update name").build(), 1L, 1L);

        //then
        assertThat(reward.getName()).isEqualTo("update name");
    }

    @DisplayName("보상 아이템 업데이트_실패")
    @Test
    void updateRewardFail() {
        //given
        Reward reward = Reward.builder().name("save name").build();

        //when
        when(rewardRepository.findById(any())).thenReturn(Optional.ofNullable(null));


        //then
        assertThatThrownBy(() -> rewardService.updateReward(RewardRequestDto.builder().name("update name").build(), 1L, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(MessageUtil.getMessage("exception.entity.notfound", MessageUtil.getMessage("item")));
    }

    @DisplayName("보상 아이템 삭제_실패")
    @Test
    public void testDeleteRewardFail() throws Exception {
        //given
        Reward reward = Reward.builder().name("save name").build();

        //when
        when(rewardRepository.findById(any())).thenReturn(Optional.ofNullable(null));

        //then
        assertThatThrownBy(() -> rewardService.deleteReward(1L, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(MessageUtil.getMessage("exception.entity.notfound", MessageUtil.getMessage("item")));
    }

}