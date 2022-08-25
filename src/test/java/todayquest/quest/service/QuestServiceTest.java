package todayquest.quest.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import todayquest.quest.dto.QuestRequestDto;
import todayquest.quest.dto.QuestResponseDto;
import todayquest.quest.entity.Quest;
import todayquest.quest.repository.QuestRepository;
import todayquest.user.dto.UserPrincipal;
import todayquest.user.entity.UserInfo;
import todayquest.user.repository.UserRepository;

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

        //when
        when(questRepository.getQuestsByUserOrderByDeadLineDateAscDeadLineTimeAsc(any())).thenReturn(List.of(Quest.builder().id(1L).title("test").build()));

        //then
        List<QuestResponseDto> result = questService.getQuestList(userId);
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).getQuestId()).isEqualTo(1L);
        assertThat(result.get(0).getTitle()).isEqualTo("test");
    }

    @Test
    public void testGetQuestInfo() throws Exception {
        //given
        Long questId = 1L;

        //when
        when(questRepository.getById(any())).thenReturn(Quest.builder().title("test").build());

        //then
        QuestResponseDto result = questService.getQuestInfo(questId);
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("test");
    }

    @Test
    public void testSaveQuest() throws Exception {
        //given
        QuestRequestDto dto = QuestRequestDto.builder().title("test").build();
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
        //when
        when(questRepository.getById(any())).thenReturn(Quest.builder().build());

        //then
        questService.updateQuest(QuestRequestDto.builder().build(), 1L);
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