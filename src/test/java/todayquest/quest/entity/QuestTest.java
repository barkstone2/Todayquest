package todayquest.quest.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import todayquest.quest.dto.QuestRequestDto;
import todayquest.user.entity.UserInfo;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class QuestTest {

    @DisplayName("클리어 보상 목록 길이 감소 테스트")
    @Test
    public void testUpdateRewardList1() {

        QuestRequestDto dto = QuestRequestDto.builder()
                .title("test title")
                .description("test description")
                .isRepeat(true)
                .deadLineDate(LocalDate.of(1111, 11, 11))
                .deadLineTime(LocalTime.of(11, 11))
                .difficulty(QuestDifficulty.easy)
                .rewards(List.of("reward1", "reward2"))
                .build();

        QuestReward reward1 = QuestReward.builder().id(1L).reward("1").build();
        QuestReward reward2 = QuestReward.builder().id(2L).reward("2").build();
        QuestReward reward3 = QuestReward.builder().id(3L).reward("3").build();
        QuestReward reward4 = QuestReward.builder().id(4L).reward("4").build();
        QuestReward reward5 = QuestReward.builder().id(5L).reward("5").build();

        List<QuestReward> rewards = new ArrayList<>(){{
           add(reward1);
           add(reward2);
           add(reward3);
           add(reward4);
           add(reward5);
        }};

        Quest entity = Quest.builder()
                .title("test")
                .description("test")
                .state(QuestState.PROCEED)
                .type(QuestType.DAILY)
                .difficulty(QuestDifficulty.easy)
                .isRepeat(true)
                .rewards(rewards)
                .user(UserInfo.builder().build())
                .deadLineDate(LocalDate.now())
                .build();

        entity.updateQuestEntity(dto);

        assertThat(entity.getRewards().size()).isEqualTo(2);
        assertThat(entity.getRewards().get(0).getId()).isEqualTo(1L);
        assertThat(entity.getRewards().get(0).getReward()).isEqualTo("reward1");
    }


    @DisplayName("클리어 보상 목록 길이 증가 테스트")
    @Test
    public void testUpdateRewardList2() {

        QuestRequestDto dto = QuestRequestDto.builder()
                .title("test title")
                .description("test description")
                .isRepeat(true)
                .deadLineDate(LocalDate.of(1111, 11, 11))
                .deadLineTime(LocalTime.of(11, 11))
                .difficulty(QuestDifficulty.easy)
                .rewards(List.of("reward1", "reward2", "reward3", "reward4"))
                .build();

        QuestReward reward1 = QuestReward.builder().id(1L).reward("1").build();
        QuestReward reward2 = QuestReward.builder().id(2L).reward("2").build();

        List<QuestReward> rewards = new ArrayList<>(){{
           add(reward1);
           add(reward2);
        }};

        Quest entity = Quest.builder()
                .title("test")
                .description("test")
                .state(QuestState.PROCEED)
                .type(QuestType.DAILY)
                .difficulty(QuestDifficulty.easy)
                .isRepeat(true)
                .rewards(rewards)
                .user(UserInfo.builder().build())
                .deadLineDate(LocalDate.now())
                .build();

        entity.updateQuestEntity(dto);

        assertThat(entity.getRewards().size()).isEqualTo(4);
        assertThat(entity.getRewards().get(0).getId()).isEqualTo(1L);
        assertThat(entity.getRewards().get(0).getReward()).isEqualTo("reward1");
        assertThat(entity.getRewards().get(3).getId()).isNull();
        assertThat(entity.getRewards().get(3).getReward()).isEqualTo("reward4");
    }



}