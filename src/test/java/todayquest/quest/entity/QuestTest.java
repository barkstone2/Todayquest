package todayquest.quest.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import todayquest.quest.dto.QuestRequestDto;
import todayquest.reward.entity.Reward;
import todayquest.user.entity.UserInfo;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("퀘스트 엔티티 유닛 테스트")
class QuestTest {

    @DisplayName("클리어 보상 목록 길이 감소 테스트")
    @Test
    public void testUpdateRewardList1() {

        //given
        QuestRequestDto dto = QuestRequestDto.builder()
                .title("test title")
                .description("test description")
                .isRepeat(true)
                .deadLineDate(LocalDate.of(1111, 11, 11))
                .deadLineTime(LocalTime.of(11, 11))
                .difficulty(QuestDifficulty.easy)
                .rewards(List.of(1L,2L,3L,4L))
                .build();

        Quest entity = Quest.builder()
                .title("test")
                .description("test")
                .state(QuestState.PROCEED)
                .type(QuestType.DAILY)
                .difficulty(QuestDifficulty.easy)
                .isRepeat(true)
                .user(UserInfo.builder().build())
                .deadLineDate(LocalDate.now())
                .build();

        Reward reward1 = Reward.builder().id(1L).name("1").build();
        Reward reward2 = Reward.builder().id(2L).name("2").build();
        Reward reward3 = Reward.builder().id(3L).name("3").build();
        Reward reward4 = Reward.builder().id(4L).name("4").build();

        List<Reward> rewards = new ArrayList<>(){{
           add(reward1);
           add(reward2);
           add(reward3);
           add(reward4);
        }};

        entity.updateQuestEntity(dto, rewards);


        //when
        rewards.remove(3);
        rewards.remove(2);

        entity.updateQuestEntity(dto, rewards);

        assertThat(entity.getRewards().size()).isEqualTo(2);
        assertThat(entity.getRewards().get(0).getReward().getId()).isEqualTo(1L);
        assertThat(entity.getRewards().get(0).getReward().getName()).isEqualTo("1");
    }


    @DisplayName("클리어 보상 목록 길이 증가 테스트")
    @Test
    public void testUpdateRewardList2() {

        //given
        QuestRequestDto dto = QuestRequestDto.builder()
                .title("test title")
                .description("test description")
                .isRepeat(true)
                .deadLineDate(LocalDate.of(1111, 11, 11))
                .deadLineTime(LocalTime.of(11, 11))
                .difficulty(QuestDifficulty.easy)
                .rewards(List.of(1L, 2L))
                .build();

        Quest entity = Quest.builder()
                .title("test")
                .description("test")
                .state(QuestState.PROCEED)
                .type(QuestType.DAILY)
                .difficulty(QuestDifficulty.easy)
                .isRepeat(true)
                .user(UserInfo.builder().build())
                .deadLineDate(LocalDate.now())
                .build();

        Reward reward1 = Reward.builder().id(1L).name("1").build();
        Reward reward2 = Reward.builder().id(2L).name("2").build();
        List<Reward> rewards = new ArrayList<>(){{
           add(reward1);
           add(reward2);
        }};

        entity.updateQuestEntity(dto, rewards);

        //when
        Reward reward3 = Reward.builder().id(3L).name("3").build();
        Reward reward4 = Reward.builder().id(4L).name("4").build();
        rewards.add(reward3);
        rewards.add(reward4);

        entity.updateQuestEntity(dto, rewards);

        assertThat(entity.getRewards().size()).isEqualTo(4);
        assertThat(entity.getRewards().get(0).getReward().getId()).isEqualTo(1L);
        assertThat(entity.getRewards().get(0).getReward().getName()).isEqualTo("1");
        assertThat(entity.getRewards().get(3).getReward().getId()).isNotNull();
        assertThat(entity.getRewards().get(3).getReward().getName()).isEqualTo("4");
    }



}