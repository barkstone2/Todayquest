package todayquest.quest.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;
import todayquest.annotation.WithCustomMockUser;
import todayquest.common.DatabaseCleanup;
import todayquest.quest.entity.Quest;
import todayquest.quest.entity.QuestDifficulty;
import todayquest.quest.entity.QuestState;
import todayquest.quest.entity.QuestType;
import todayquest.quest.repository.QuestRepository;
import todayquest.user.entity.UserInfo;
import todayquest.user.repository.UserRepository;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@DisplayName("퀘스트 API 컨트롤러 통합 테스트")
@WithCustomMockUser(userId = 1L)
@Transactional
@SpringBootTest(webEnvironment = RANDOM_PORT)
public class QuestApiControllerTest {

    @LocalServerPort
    int port;

    @Autowired
    QuestRepository questRepository;

    @Autowired
    UserRepository userRepository;


    @Autowired
    WebApplicationContext context;

    @Autowired
    DatabaseCleanup databaseCleanup;

    MockMvc mvc;

    UserInfo testUser;
    Quest testQuest;
    static final String SERVER_ADDR = "http://localhost:";
    static final String URI_PREFIX = "/api/quests";

    @BeforeEach
    public void setUp() {

        databaseCleanup.execute();

        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .addFilter(new CharacterEncodingFilter("UTF-8", true))
                .apply(springSecurity())
                .build();

        testUser = userRepository.getById(1L);
        testQuest = questRepository.save(
                Quest.builder()
                        .user(testUser).title("t")
                        .description("d").state(QuestState.PROCEED)
                        .seq(1L)
                        .difficulty(QuestDifficulty.easy)
                        .type(QuestType.DAILY)
                        .isRepeat(true)
                        .build());
    }

    @DisplayName("퀘스트 목록 화면 API 요청 통합 테스트")
    @Test
    public void testList() throws Exception {
        //given
        String url = SERVER_ADDR + port + URI_PREFIX + "/";
        int page = 0;

        //when
        String body = mvc.perform(get(url).queryParam("page", String.valueOf(page)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn().getResponse().getContentAsString();

        ObjectMapper om = new ObjectMapper();
        Map<String, Object> result = om.readValue(body, new TypeReference<>() {
        });

        log.info("result = {}", result);

        //then
        assertThat(((List)result.get("content")).size()).isEqualTo(1);
        assertThat(result.get("number")).isEqualTo(page);

    }


    @DisplayName("퀘스트 목록 화면 API 요청_페이징 통합 테스트")
    @Test
    public void testListWithPaging() throws Exception {
        //given
        String url = SERVER_ADDR + port + URI_PREFIX + "/";
        int page = 1;

        //when
        String body = mvc.perform(get(url)
                        .queryParam("page", String.valueOf(page)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn().getResponse().getContentAsString();

        ObjectMapper om = new ObjectMapper();
        Map<String, Object> result = om.readValue(body, new TypeReference<>() {
        });

        //then
        assertThat(((List)result.get("content")).size()).isEqualTo(0);
        assertThat(result.get("number")).isEqualTo(page);

    }



}
