package todayquest.quest.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import todayquest.quest.dto.QuestRequestDto;
import todayquest.quest.dto.QuestResponseDto;
import todayquest.quest.entity.Quest;
import todayquest.quest.entity.QuestDifficulty;
import todayquest.quest.entity.QuestState;
import todayquest.quest.entity.QuestType;
import todayquest.quest.repository.QuestRepository;
import todayquest.user.dto.UserPrincipal;
import todayquest.user.entity.UserInfo;
import todayquest.user.repository.UserRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuestServiceTest {

    @InjectMocks
    QuestService questService;

    @Mock
    QuestRepository questRepository;

    @Mock
    UserRepository userRepository;



    @Test
    public void testGetList() throws Exception {
        //given
        Long userId = 1L;
        List<Quest> entityList = List.of(
                Quest.builder()
                        .title("test")
                        .description("test")
                        .state(QuestState.PROCEED)
                        .type(QuestType.DAILY)
                        .difficulty(QuestDifficulty.easy)
                        .isRepeat(true)
                        .user(UserInfo.builder().build())
                        .rewards(new ArrayList<>())
                        .deadLineDate(LocalDate.now())
                        .build());

        //when
        when(questRepository.getQuestsByUserOrderByDeadLineDateAscDeadLineTimeAsc(any())).thenReturn(entityList);

        //then
        List<QuestResponseDto> result = questService.getQuestList(userId);
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).getTitle()).isEqualTo("test");
    }

    @Test
    public void testGetQuestInfo() throws Exception {
        //given
        Long questId = 1L;
        Quest entity = Quest.builder()
                .title("test")
                .description("test")
                .state(QuestState.PROCEED)
                .type(QuestType.DAILY)
                .difficulty(QuestDifficulty.easy)
                .isRepeat(true)
                .rewards(new ArrayList<>())
                .deadLineDate(LocalDate.now())
                .user(UserInfo.builder().build())
                .build();

        //when
        when(questRepository.getById(any())).thenReturn(entity);

        //then
        QuestResponseDto result = questService.getQuestInfo(questId);
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("test");
    }

    @Test
    public void testSaveQuest() throws Exception {
        //given
        QuestRequestDto dto = QuestRequestDto.builder()
                .title("test")
                .description("test")
                .difficulty(QuestDifficulty.easy)
                .isRepeat(true)
                .deadLineDate(LocalDate.now())
                .deadLineTime(LocalTime.now())
                .rewards(new ArrayList<>())
                .build();
        UserPrincipal principal = UserPrincipal.builder().build();

        //when
        when(userRepository.getById(any())).thenReturn(UserInfo.builder().build());

        //then
        questService.saveQuest(dto, principal);
    }

    @Test
    public void testUpdateQuest() throws Exception {
        //given
        Long questId = 1L;
        Quest entity = Quest.builder()
                .title("test")
                .description("test")
                .state(QuestState.PROCEED)
                .type(QuestType.DAILY)
                .difficulty(QuestDifficulty.easy)
                .isRepeat(true)
                .rewards(new ArrayList<>())
                .user(UserInfo.builder().build())
                .deadLineDate(LocalDate.now())
                .build();

        //when
        when(questRepository.getById(any())).thenReturn(entity);
        //then
        QuestRequestDto dto = QuestRequestDto.builder()
                .title("test")
                .description("test")
                .difficulty(QuestDifficulty.easy)
                .isRepeat(true)
                .deadLineDate(LocalDate.now())
                .deadLineTime(LocalTime.now())
                .rewards(new ArrayList<>())
                .build();

        questService.updateQuest(dto, 1L);
    }


    @Test
    public void testDeleteQuest() throws Exception {
        //given
        Long questId = 1L;

        //when
        //then
        questService.deleteQuest(questId);
    }

}