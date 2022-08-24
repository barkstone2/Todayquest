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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import todayquest.annotation.WithCustomMockUser;
import todayquest.config.SecurityConfig;
import todayquest.quest.dto.QuestRequestDto;
import todayquest.quest.dto.QuestResponseDto;
import todayquest.quest.entity.QuestState;
import todayquest.quest.service.QuestService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Slf4j
@WithCustomMockUser
@WebMvcTest(controllers = QuestController.class,
excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = SecurityConfig.class
    )
)
class QuestControllerUnitTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    QuestService questService;

    static final String URI_PREFIX = "/quests";
    List<QuestResponseDto> list = new ArrayList<>();
    ObjectMapper mapper;

    @BeforeEach
    void init() {
        QuestResponseDto quest1 = QuestResponseDto.builder()
                .questId(1L).state(QuestState.PROCEED).title("title1")
                .description("desc1").isRepeat(true)
                .deadLineDate(LocalDate.now()).deadLineTime(LocalTime.now())
                .build();

        QuestResponseDto quest2 = QuestResponseDto.builder()
                .questId(2L).state(QuestState.PROCEED).title("title2")
                .description("desc2").isRepeat(true)
                .deadLineDate(LocalDate.now()).deadLineTime(LocalTime.now())
                .build();

        list.add(quest1);
        list.add(quest2);
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
    }


    @DisplayName("퀘스트 목록 화면 요청")
    @Test
    public void testList() throws Exception {
        //given
        String uri = "";

        //when
        when(questService.getQuestList(any())).thenReturn(list);

        //then
        mvc.perform(get(URI_PREFIX + uri))
                .andExpect(status().isOk())
                .andExpect(model().attribute("questList", list))
                .andExpect(view().name("/quest/list"));
    }

    @DisplayName("퀘스트 등록 화면 요청")
    @Test
    public void testSaveForm() throws Exception {
        //given
        String uri = "/save";

        //when
        //then
        mvc.perform(get(URI_PREFIX + uri))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("quest"))
                .andExpect(view().name("/quest/save"));

    }

    @DisplayName("퀘스트 조회 화면 요청")
    @Test
    public void testView() throws Exception {
        //given
        String uri = "/" + list.get(0).getQuestId();

        //when
        when(questService.getQuestInfo(any())).thenReturn(list.get(0));

        //then
        mvc.perform(get(URI_PREFIX + uri))
                .andExpect(status().isOk())
                .andExpect(model().attribute("quest", list.get(0)))
                .andExpect(view().name("/quest/view"));
    }

    @DisplayName("퀘스트 등록 요청")
    @Test
    public void testSave() throws Exception {
        //given
        String uri = "/save";
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("title", "save title");
        map.add("description", "save description");
        map.add("deadLineDate", "2022-11-11");
        map.add("deadLineTime", "11:11");
        map.add("repeat", "true");

        //when
        doNothing()
                .when(questService)
                .saveQuest(any(QuestRequestDto.class), any());

        //then
        mvc.perform(
                    post(URI_PREFIX + uri)
                        .with(csrf())
                        .params(map)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/quests"));

    }

    @DisplayName("퀘스트 삭제 요청")
    @Test
    public void testDelete() throws Exception {
        //given
        String uri = "/" + list.get(0).getQuestId();

        //when
        //then
        mvc.perform(delete(URI_PREFIX + uri).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/quests"));
    }


    @DisplayName("퀘스트 수정 요청")
    @Test
    public void testUpdate() throws Exception {
        //given
        Long questId = list.get(0).getQuestId();
        String uri = "/" + questId;

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("title", "save title");
        map.add("description", "save description");
        map.add("deadLineDate", "2022-11-11");
        map.add("deadLineTime", "11:11");
        map.add("repeat", "true");

        //when

        doNothing()
                .when(questService)
                .updateQuest(any(QuestRequestDto.class), any());

        //then
        mvc.perform(
                put(URI_PREFIX + uri)
                        .with(csrf())
                        .params(map)
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/quests/" + questId));
    }


}