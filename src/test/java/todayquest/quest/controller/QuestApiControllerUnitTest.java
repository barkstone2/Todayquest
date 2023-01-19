package todayquest.quest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import todayquest.annotation.WithCustomMockUser;
import todayquest.config.SecurityConfig;
import todayquest.quest.dto.QuestResponseDto;
import todayquest.quest.entity.QuestDifficulty;
import todayquest.quest.entity.QuestState;
import todayquest.quest.service.QuestService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@DisplayName("퀘스트 API 컨트롤러 유닛 테스트")
@WithCustomMockUser
@WebMvcTest(controllers = QuestApiController.class,
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = SecurityConfig.class
    )
)
public class QuestApiControllerUnitTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    QuestService questService;

    static final String URI_PREFIX = "/api/quests";
    Slice<QuestResponseDto> questList;
    ObjectMapper mapper;

    @BeforeEach
    void init() {
        QuestResponseDto quest1 = QuestResponseDto.builder().questId(1L).state(QuestState.PROCEED).title("title1").description("desc1").isRepeat(true).deadLineDate(LocalDate.now()).deadLineTime(LocalTime.now()).difficulty(QuestDifficulty.EASY).state(QuestState.PROCEED).rewards(new ArrayList<>()).build();

        QuestResponseDto quest2 = QuestResponseDto.builder().questId(2L).state(QuestState.PROCEED).title("title2").description("desc2").isRepeat(true).deadLineDate(LocalDate.now()).deadLineTime(LocalTime.now()).difficulty(QuestDifficulty.EASY).state(QuestState.PROCEED).rewards(new ArrayList<>()).build();

        List<QuestResponseDto> list = new LinkedList<>();
        list.add(quest1);
        list.add(quest2);

        questList = new SliceImpl<>(list);
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
    }


    @DisplayName("퀘스트 목록 API 조회")
    @Test
    public void testGetApiList() throws Exception {
        //given
        String uri = "";

        //when
        when(questService.getQuestList(any(), any(), any())).thenReturn(questList);

        //then
        mvc.perform(get(URI_PREFIX + uri))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

}
