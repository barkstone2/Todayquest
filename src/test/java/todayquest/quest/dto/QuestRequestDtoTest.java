package todayquest.quest.dto;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import todayquest.quest.entity.Quest;
import todayquest.user.entity.ProviderType;
import todayquest.user.entity.UserInfo;

import java.time.LocalDate;
import java.time.LocalTime;

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

        dto = new QuestRequestDto();
        dto.setRepeat(true);
        dto.setTitle("test title");
        dto.setDescription("test description");
        dto.setDeadLineDate(LocalDate.of(1111, 11, 11));
        dto.setDeadLineTime(LocalTime.of(11, 11));
    }

    @DisplayName("Entity 객체 생성 Help Method 테스트")
    @Test
    public void testMapToEntity() throws Exception {
        Quest givenEntity = dto.mapToEntity(userInfo);

        assertThat(givenEntity.isRepeat()).isEqualTo(dto.isRepeat());
        assertThat(givenEntity.getTitle()).isEqualTo(dto.getTitle());
        assertThat(givenEntity.getDescription()).isEqualTo(dto.getDescription());
        assertThat(givenEntity.getDeadLineDate()).isEqualTo(dto.getDeadLineDate());
        assertThat(givenEntity.getDeadLineTime()).isEqualTo(dto.getDeadLineTime());
        assertThat(givenEntity.getUser()).isEqualTo(userInfo);

    }
}