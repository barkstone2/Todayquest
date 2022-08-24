package todayquest.quest.dto;

import org.junit.jupiter.api.Test;
import todayquest.quest.entity.Quest;

import static org.assertj.core.api.Assertions.assertThat;

class QuestResponseDtoTest {

    @Test
    public void testQuestEntityCreate() throws Exception {
        //given
        Quest quest = Quest.builder().id(1L).isRepeat(true).title("title").description("description").build();

        //when
        QuestResponseDto dto = QuestResponseDto.createDto(quest);

        //then
        assertThat(quest.getTitle()).isEqualTo(dto.getTitle());
        assertThat(quest.getId()).isEqualTo(dto.getQuestId());
    }

}