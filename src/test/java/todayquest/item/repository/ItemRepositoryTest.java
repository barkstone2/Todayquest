package todayquest.item.repository;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import todayquest.item.entity.Item;
import todayquest.reward.entity.Reward;
import todayquest.reward.entity.RewardGrade;
import todayquest.reward.repository.RewardRepository;
import todayquest.user.entity.UserInfo;
import todayquest.user.repository.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;


@DisplayName("아이템 리포지토리 테스트")
@DataJpaTest
public class ItemRepositoryTest {

    @Autowired
    ItemRepository itemRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RewardRepository rewardRepository;

    @DisplayName("아이템 목록 조회 테스트")
    @Test
    public void testFindByUserIdAndCountIsNot() throws Exception {
        //given
        Long userId = 1L;
        int count = 0;

        UserInfo user = userRepository.getById(userId);

        Reward r1 = rewardRepository.save(Reward.builder()
                .name("r1").user(user).grade(RewardGrade.E)
                .build());
        Reward r2 = rewardRepository.save(Reward.builder()
                .name("r1").user(user).grade(RewardGrade.E)
                .build());
        Reward r3 = rewardRepository.save(Reward.builder()
                .name("r1").user(user).grade(RewardGrade.E)
                .build());

        Item i1 = Item.builder().user(user).reward(r1).count(1).build();
        Item i2 = Item.builder().user(user).reward(r2).count(1).build();
        Item i3 = Item.builder().user(user).reward(r3).count(0).build();

        i1 = itemRepository.save(i1);
        i2 = itemRepository.save(i2);
        i3 = itemRepository.save(i3);

        //when
        List<Item> items = itemRepository.findByUserIdAndCountIsNot(userId, count);

        //then
        assertThat(items.size()).isEqualTo(2);
        assertThat(items).contains(i1, i2);
        assertThat(items).doesNotContain(i3);
    }


    @DisplayName("아이템 ID와 유저 ID로 아이템 조회")
    @Test
    public void testFindByIdAndUserId() throws Exception {
        //given
        Long userId = 1L;

        UserInfo user = userRepository.getById(userId);

        Reward r1 = rewardRepository.save(
                Reward.builder()
                        .name("r1")
                        .user(user)
                        .grade(RewardGrade.E)
                        .build());
        Item i1 = itemRepository.save(
                Item.builder()
                        .user(user)
                        .reward(r1)
                        .count(1)
                        .build());

        //when
        Optional<Item> findItem = itemRepository.findByIdAndUserId(i1.getId(), userId);

        //then
        assertThat(findItem).isNotEmpty();
        assertThat(findItem.get().getReward()).isEqualTo(r1);
    }


    @DisplayName("리워드 ids와 유저 ID로 아이템 조회")
    @Test
    public void testFindAllByRewardIdsAndUserId() throws Exception {
        //given
        Long userId = 1L;
        UserInfo user = userRepository.getById(userId);
        UserInfo anotherUser = UserInfo.builder().id(2L).build();

        Reward r1 = rewardRepository.save(Reward.builder()
                .name("r1").user(user).grade(RewardGrade.E)
                .build());
        Reward r2 = rewardRepository.save(Reward.builder()
                .name("r1").user(user).grade(RewardGrade.E)
                .build());
        Reward r3 = rewardRepository.save(Reward.builder()
                .name("r1").user(anotherUser).grade(RewardGrade.E)
                .build());

        Item i1 = Item.builder().user(user).reward(r1).count(1).build();
        Item i2 = Item.builder().user(user).reward(r2).count(1).build();
        Item i3 = Item.builder().user(anotherUser).reward(r3).count(0).build();

        i1 = itemRepository.save(i1);
        i2 = itemRepository.save(i2);
        i3 = itemRepository.save(i3);

        //when
        List<Item> items = itemRepository.findAllByRewardIdsAndUserId(
                List.of(r1.getId(), r2.getId(), r3.getId()),
                userId
        );

        //then
        assertThat(items.size()).isEqualTo(2);
        assertThat(items).contains(i1, i2);
        assertThat(items).doesNotContain(i3);
    }


}
