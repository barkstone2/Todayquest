package todayquest.item.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import todayquest.common.MessageUtil;
import todayquest.item.dto.ItemResponseDto;
import todayquest.item.entity.Item;
import todayquest.item.entity.ItemLogType;
import todayquest.item.repository.ItemRepository;
import todayquest.reward.entity.Reward;
import todayquest.reward.entity.RewardGrade;
import todayquest.user.entity.UserInfo;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import static org.mockito.Mockito.*;

@DisplayName("아이템 서비스 유닛 테스트")
@ExtendWith(MockitoExtension.class)
public class ItemServiceTest {

    @InjectMocks
    ItemService itemService;
    @Mock ItemRepository itemRepository;
    @Mock ItemLogService itemLogService;

    @InjectMocks MessageUtil messageUtil;
    @Mock MessageSource messageSource;

    Item item;

    @BeforeEach
    void setUp() {
        Reward r1 = Reward.builder()
                .id(1L).name("r1").grade(RewardGrade.E).build();
        item = Item.builder().reward(r1).count(1).build();
    }



    @DisplayName("인벤토리 아이템 목록 조회")
    @Test
    public void testGetInventoryItems() throws Exception {
        //given
        Long userId = 1L;

        when(itemRepository.findByUserIdAndCountIsNot(userId, 0)).thenReturn(List.of(item));

        //when
        List<ItemResponseDto> result = itemService.getInventoryItems(userId);

        //then
        verify(itemRepository).findByUserIdAndCountIsNot(userId, 0);
        assertThat(result.size()).isEqualTo(1);
    }

    @DisplayName("아이템 정보 조회")
    @Test
    public void testGetItemInfo() throws Exception {
        //given
        Long userId = 1L;
        Long itemId = item.getId();
        when(itemRepository.findByIdAndUserId(itemId, userId))
                .thenReturn(Optional.ofNullable(item));

        //when
        ItemResponseDto result = itemService.getItemInfo(itemId, userId);

        //then
        verify(itemRepository).findByIdAndUserId(itemId, userId);
        assertThat(result.getName()).isEqualTo(item.getReward().getName());
    }

    @DisplayName("아이템 정보 조회_엔티티 정보 없음")
    @Test
    public void testGetItemInfoNotFound() throws Exception {
        //given
        Long userId = 1L;
        Long itemId = item.getId();
        when(itemRepository.findByIdAndUserId(itemId, userId))
                .thenReturn(Optional.ofNullable(null));

        //when
        ThrowingCallable tc = () -> itemService.getItemInfo(itemId, userId);

        //then
        assertThatThrownBy(tc)
                .isInstanceOf(IllegalArgumentException.class)
                        .hasMessage(MessageUtil.getMessage("exception.entity.notfound", MessageUtil.getMessage("reward")));

        verify(itemRepository).findByIdAndUserId(itemId, userId);
    }


    @DisplayName("아이템 사용")
    @Test
    public void testUseItem() throws Exception {
        //given
        Long itemId = item.getId();
        Long userId = 1L;
        int count = 1;
        when(itemRepository.findByIdAndUserId(itemId, userId))
                .thenReturn(Optional.ofNullable(item));

        //when
        ItemResponseDto usedItem = itemService.useItem(itemId, userId, count);

        //then
        verify(itemRepository).findByIdAndUserId(itemId, userId);
        verify(itemLogService).saveItemLogs(item.getReward().getId(), userId, ItemLogType.USE);
        assertThat(usedItem.getCount()).isEqualTo(0);
        assertThat(usedItem.getName()).isEqualTo(item.getReward().getName());
    }


    @DisplayName("아이템 사용_남은 수량 부족")
    @Test
    public void testUseItemNotEnough() throws Exception {
        //given
        Long itemId = item.getId();
        Long userId = 1L;
        int count = 2;
        when(itemRepository.findByIdAndUserId(itemId, userId))
                .thenReturn(Optional.ofNullable(item));

        //when
        ThrowingCallable tc = () -> itemService.useItem(itemId, userId, count);

        //then
        assertThatThrownBy(tc)
                .isInstanceOf(IllegalArgumentException.class);

        verify(itemRepository).findByIdAndUserId(itemId, userId);
        verifyNoInteractions(itemLogService);
        assertThat(item.getCount()).isEqualTo(1);
    }

    @DisplayName("아이템 버리기")
    @Test
    public void testAbandonItem() throws Exception {
        //given
        Long itemId = item.getId();
        Long userId = 1L;
        int count = 1;
        when(itemRepository.findByIdAndUserId(itemId, userId))
                .thenReturn(Optional.ofNullable(item));

        //when
        ItemResponseDto usedItem = itemService.abandonItem(itemId, userId, count);

        //then
        verify(itemRepository).findByIdAndUserId(itemId, userId);
        verify(itemLogService).saveItemLogs(item.getReward().getId(), userId, ItemLogType.ABANDON);
        assertThat(usedItem.getCount()).isEqualTo(0);
        assertThat(usedItem.getName()).isEqualTo(item.getReward().getName());
    }

    @DisplayName("퀘스트 완료 후 아이템 등록 처리")
    @Test
    public void testSaveAllWithDirtyChecking() throws Exception {
        //given
        Long userId = 1L;
        UserInfo userInfo = UserInfo.builder().id(userId).build();
        Reward newReward = Reward.builder().id(4L).build();

        List<Reward> rewards = List.of(
                Reward.builder().id(1L).build(),
                Reward.builder().id(2L).build(),
                Reward.builder().id(3L).build(),
                newReward
        );


        List<Long> rewardIds = List.of(1L, 2L, 3L, 4L);

        List<Item> items = List.of(
                Item.builder().reward(newReward).count(1).build()
        );

        when(itemRepository.findAllByRewardIdsAndUserId(rewardIds, userId))
                .thenReturn(items);


        //when
        itemService.saveAllWithDirtyChecking(rewards, userInfo);

        //then
        verify(itemRepository).findAllByRewardIdsAndUserId(rewardIds, userId);
        verify(itemRepository).saveAll(anyList());
        verify(itemLogService).saveItemEarnLogs(rewardIds, userId);

        assertThat(items.get(0).getCount()).isEqualTo(2);
    }

}
