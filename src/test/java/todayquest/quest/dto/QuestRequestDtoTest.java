package todayquest.quest.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import todayquest.quest.entity.QuestDifficulty;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@DisplayName("퀘스트 요청 DTO 테스트")
class QuestRequestDtoTest {

    @DisplayName("Builder 테스트")
    @Test
    public void testBuilderSuccess() throws Exception {
        QuestRequestDto.builder()
                .title("test title")
                .description("test description")
                .isRepeat(true)
                .deadLineDate(LocalDate.of(1111, 11, 11))
                .deadLineTime(LocalTime.of(11, 11))
                .difficulty(QuestDifficulty.EASY)
                .rewards(List.of(1L))
                .build();
    }


}