package todayquest.reward.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.security.access.AccessDeniedException;
import todayquest.common.MessageUtil;
import todayquest.reward.dto.RewardRequestDto;
import todayquest.reward.dto.RewardResponseDto;
import todayquest.reward.entity.Reward;
import todayquest.reward.entity.RewardGrade;
import todayquest.reward.repository.RewardRepository;
import todayquest.user.entity.UserInfo;
import todayquest.user.repository.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
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
        long userId = 1L;
        when(rewardRepository.findAllByUserId(any())).thenReturn(list);

        //when
        List<RewardResponseDto> rewardList = rewardService.getRewardList(userId);

        //then
        verify(rewardRepository).findAllByUserId(userId);
        assertThat(rewardList.size()).isEqualTo(1);
        assertThat(rewardList.get(0).getName()).isEqualTo(list.get(0).getName());
    }

    @DisplayName("보상 아이템 조회")
    @Test
    void getReward() {
        //given
        long rewardId = 1L;
        long userId = 1L;

        Reward reward = Reward.builder()
                .name("name")
                .user(UserInfo.builder().id(userId).build())
                .build();

        when(rewardRepository.findByIdNotDeleted(rewardId)).thenReturn(Optional.ofNullable(reward));

        //when
        RewardResponseDto dto = rewardService.getReward(rewardId, userId);

        //then
        verify(rewardRepository).findByIdNotDeleted(rewardId);
        assertThat(dto.getName()).isEqualTo(reward.getName());
    }

    @DisplayName("보상 아이템 조회 시 NULL 반환")
    @Test
    void getRewardFail() {
        //given
        long rewardId = 1L;
        long userId = 1L;

        when(rewardRepository.findByIdNotDeleted(rewardId)).thenReturn(Optional.ofNullable(null));

        //when
        ThrowingCallable tc = () -> rewardService.getReward(rewardId, userId);

        //then
        assertThatThrownBy(tc)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(MessageUtil.getMessage("exception.entity.notfound", MessageUtil.getMessage("item")));

        verify(rewardRepository).findByIdNotDeleted(rewardId);
    }


    @DisplayName("다른 유저의 Reward Entity 요청")
    @Test
    void getOtherUsersReward() {
        //given
        long rewardId = 1L;
        long userId = 1L;

        Reward reward = Reward.builder()
                .name("name")
                .user(UserInfo.builder().id(2L).build())
                .build();

        when(rewardRepository.findByIdNotDeleted(rewardId)).thenReturn(Optional.ofNullable(reward));

        //when
        ThrowingCallable tc = () -> rewardService.getReward(rewardId, userId);

        //then
        assertThatThrownBy(tc)
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage(MessageUtil.getMessage("exception.access.denied", MessageUtil.getMessage("reward")));

        verify(rewardRepository).findByIdNotDeleted(rewardId);
    }

    @DisplayName("보상 아이템 저장")
    @Test
    void saveReward() {
        //given
        Long userId = 1L;
        RewardRequestDto dto = RewardRequestDto.builder()
                .name("save name").build();

        Reward reward = Reward.builder().id(1L).build();
        UserInfo user = UserInfo.builder().id(userId).build();

        when(userRepository.getById(userId))
                .thenReturn(user);
        when(rewardRepository.save(any(Reward.class)))
                .thenReturn(reward);

        //when
        rewardService.saveReward(dto, userId);

        //then
        verify(userRepository).getById(userId);
        verify(rewardRepository).save(any(Reward.class));
    }

    @DisplayName("보상 아이템 업데이트")
    @Test
    void updateReward() {
        //given
        Long rewardId = 1L;
        Long userId = 1L;
        RewardRequestDto dto = RewardRequestDto.builder()
                .name("update name").build();

        Reward reward = Reward.builder()
                .id(rewardId).name("save name")
                .user(UserInfo.builder().id(userId).build())
                .build();

        //when
        when(rewardRepository.findByIdNotDeleted(rewardId))
                .thenReturn(Optional.ofNullable(reward));
        rewardService.updateReward(dto, rewardId, userId);

        //then
        verify(rewardRepository).findByIdNotDeleted(rewardId);
        assertThat(reward.getName()).isEqualTo("update name");
    }

    @DisplayName("보상 아이템 삭제")
    @Test
    public void testDeleteReward() throws Exception {
        //given
        Long rewardId = 1L;
        Long userId = 1L;
        Reward reward = Reward.builder()
                .id(rewardId).name("save name")
                .user(UserInfo.builder().id(userId).build())
                .build();

        when(rewardRepository.findByIdNotDeleted(rewardId))
                .thenReturn(Optional.ofNullable(reward));

        when(rewardRepository.isRewardUseInProceedQuest(rewardId))
                .thenReturn(false);

        //when
        rewardService.deleteReward(rewardId, userId);

        //then
        verify(rewardRepository).findByIdNotDeleted(rewardId);
        verify(rewardRepository).isRewardUseInProceedQuest(rewardId);
        assertThat(reward.isDeleted()).isTrue();
    }


    @DisplayName("보상 아이템 삭제_Proceed 퀘스트에서 사용중")
    @Test
    public void testDeleteRewardUsed() throws Exception {
        //given
        Long rewardId = 1L;
        Long userId = 1L;
        Reward reward = Reward.builder()
                .id(rewardId).name("save name")
                .user(UserInfo.builder().id(userId).build())
                .build();

        when(rewardRepository.findByIdNotDeleted(rewardId))
                .thenReturn(Optional.ofNullable(reward));

        when(rewardRepository.isRewardUseInProceedQuest(rewardId))
                .thenReturn(true);

        //when
        ThrowingCallable tc = () -> rewardService.deleteReward(rewardId, userId);

        //then
        assertThatThrownBy(tc)
                .isInstanceOf(IllegalStateException.class)
                        .hasMessage(MessageUtil.getMessage("reward.error.delete.used"));

        verify(rewardRepository).findByIdNotDeleted(rewardId);
        verify(rewardRepository).isRewardUseInProceedQuest(rewardId);
        assertThat(reward.isDeleted()).isFalse();

    }


    @DisplayName("Reward Ids로 리워드 목록 조회")
    @Test
    public void testGetRewardListByIds() throws Exception {
        //given
        long rId1 = 1L;
        long rId2 = 2L;
        long rId3 = 3L;
        List<Long> ids = List.of(rId1, rId2, rId3);
        Long userId = 1L;

        List<Reward> rewards = List.of(
                Reward.builder()
                        .id(rId1).name("n")
                        .description("d").grade(RewardGrade.E).build(),
                Reward.builder()
                        .id(rId2).name("n")
                        .description("d").grade(RewardGrade.E).build(),
                Reward.builder()
                        .id(rId3).name("n")
                        .description("d").grade(RewardGrade.E).build()
        );

        when(rewardRepository.findAllByIdAndUserId(ids, userId))
                .thenReturn(rewards);


        //when
        List<RewardResponseDto> result = rewardService.getRewardListByIds(ids, userId);

        //then
        verify(rewardRepository).findAllByIdAndUserId(ids, userId);
        assertThat(result.size()).isEqualTo(3);
        assertThat(result).map(r -> r.getId()).contains(rId1, rId2, rId3);
    }



}