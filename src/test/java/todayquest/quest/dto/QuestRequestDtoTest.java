package todayquest.quest.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import todayquest.quest.entity.QuestDifficulty;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class QuestRequestDtoTest {

    @DisplayName("Builder 테스트, rewards 크기가 5를 넘지 않을 때")
    @Test
    public void testBuilderSuccess() throws Exception {
        QuestRequestDto.builder()
                .title("test title")
                .description("test description")
                .isRepeat(true)
                .deadLineDate(LocalDate.of(1111, 11, 11))
                .deadLineTime(LocalTime.of(11, 11))
                .difficulty(QuestDifficulty.easy)
                .rewards(List.of(1L))
                .build();
    }

    @DisplayName("Builder 테스트, rewards 크기가 5를 넘을 때")
    @Test
    public void testBuilderFail() throws Exception {
        assertThatThrownBy(() -> QuestRequestDto.builder()
                .title("test title")
                .description("test description")
                .isRepeat(true)
                .deadLineDate(LocalDate.of(1111, 11, 11))
                .deadLineTime(LocalTime.of(11, 11))
                .difficulty(QuestDifficulty.easy)
                .rewards(List.of(1L,2L,3L,4L,5L,6L))
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("rewards size must not exceed 5");
    }



}