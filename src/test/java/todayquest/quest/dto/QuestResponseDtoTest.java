package todayquest.quest.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import todayquest.quest.entity.Quest;
import todayquest.quest.entity.QuestDifficulty;
import todayquest.quest.entity.QuestState;
import todayquest.quest.entity.QuestType;
import todayquest.user.entity.UserInfo;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("퀘스트 응답 DTO 테스트")
class QuestResponseDtoTest {

    @Test
    public void testQuestEntityCreate() throws Exception {
        //given
        Quest quest = Quest.builder()
                .title("title")
                .description("description")
                .isRepeat(true)
                .deadLineDate(LocalDate.now())
                .user(UserInfo.builder().build())
                .state(QuestState.PROCEED)
                .type(QuestType.DAILY)
                .difficulty(QuestDifficulty.EASY)
                .build();

        //when
        QuestResponseDto dto = QuestResponseDto.createDto(quest);

        //then
        assertThat(quest.getTitle()).isEqualTo(dto.getTitle());
        assertThat(quest.getId()).isEqualTo(dto.getQuestId());
    }

}