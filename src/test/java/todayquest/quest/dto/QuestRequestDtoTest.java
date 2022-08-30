package todayquest.quest.dto;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;
import todayquest.quest.entity.Quest;
import todayquest.quest.entity.QuestDifficulty;
import todayquest.user.entity.ProviderType;
import todayquest.user.entity.UserInfo;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class QuestRequestDtoTest {

    private UserInfo userInfo;
    private QuestRequestDto dto;

    @BeforeEach
    void init() {
        userInfo = UserInfo.builder()
                .id(1L)
                .providerType(ProviderType.GOOGLE)
                .nickname("test")
                .oauth2Id("test-id-1111")
                .build();

        dto = QuestRequestDto.builder()
                .title("test title")
                .description("test description")
                .isRepeat(true)
                .deadLineDate(LocalDate.of(1111, 11, 11))
                .deadLineTime(LocalTime.of(11, 11))
                .difficulty(QuestDifficulty.easy)
                .rewards(List.of("reward1", "reward2"))
                .build();
    }

    @DisplayName("MapToEntity 테스트, rewards 크기가 5를 넘지 않을 때")
    @Test
    public void testMapToEntitySuccess() throws Exception {
        Quest givenEntity = dto.mapToEntity(userInfo);

        assertThat(givenEntity.isRepeat()).isEqualTo(dto.isRepeat());
        assertThat(givenEntity.getTitle()).isEqualTo(dto.getTitle());
        assertThat(givenEntity.getDescription()).isEqualTo(dto.getDescription());
        assertThat(givenEntity.getDeadLineDate()).isEqualTo(dto.getDeadLineDate());
        assertThat(givenEntity.getDeadLineTime()).isEqualTo(dto.getDeadLineTime());
        assertThat(givenEntity.getUser()).isEqualTo(userInfo);

    }

    @DisplayName("MapToEntity 테스트, rewards 크기가 5를 넘을 때")
    @Test
    public void testMapToEntityFail() throws Exception {
        dto.setRewards(List.of("1","2","3","4","5","6"));
        assertThatThrownBy(() -> dto.mapToEntity(userInfo))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("rewards size must not exceed 5");
    }



}