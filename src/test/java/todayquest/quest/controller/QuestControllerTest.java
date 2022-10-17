package todayquest.quest.controller;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.WebApplicationContext;
import todayquest.annotation.WithCustomMockUser;
import todayquest.quest.entity.Quest;
import todayquest.quest.entity.QuestDifficulty;
import todayquest.quest.entity.QuestState;
import todayquest.quest.entity.QuestType;
import todayquest.quest.repository.QuestRepository;
import todayquest.user.entity.DifficultyType;
import todayquest.user.entity.ProviderType;
import todayquest.user.entity.UserInfo;
import todayquest.user.repository.UserRepository;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Slf4j
@WithCustomMockUser(userId = 1L)
@Transactional
@SpringBootTest(webEnvironment = RANDOM_PORT)
class QuestControllerTest {

    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate restTemplate;

    @Autowired
    QuestRepository questRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    QuestController questController;

    static final String URI_PREFIX = "/quests";

    @Autowired
    WebApplicationContext context;

    MockMvc mvc;

    UserInfo testUser;

    @BeforeEach
    public void setUp() {
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        testUser = UserInfo.builder().id(1L).build();
    }

    @DisplayName("퀘스트 목록 화면 요청 통합 테스트")
    @Test
    public void testList() throws Exception {
        //given
        String url = "http://localhost:" + port + URI_PREFIX + "/";

        //when
        String body = mvc.perform(get(url))
                .andExpect(status().isOk())
                .andExpect(view().name("/quest/list"))
                .andExpect(model().attributeExists("questList"))
                .andReturn().getResponse().getContentAsString();

        //then
        assertThat(body).contains("퀘스트 목록");
    }


    @DisplayName("퀘스트 등록 요청 통합 테스트")
    @Test
    public void testSave() throws Exception {
        //given
        String url = "http://localhost:" + port + URI_PREFIX + "/save";

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("title", "save title");
        map.add("description", "save description");
        map.add("deadLineDate", "2022-11-11");
        map.add("deadLineTime", "11:11");
        map.add("repeat", "true");
        map.add("difficulty", "easy");
        map.add("rewards", "");

        //when
        mvc.perform(
                        post(url)
                                .with(csrf())
                                .params(map)
                                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/quests"));


        //then
        List<Quest> list = questRepository.getQuestsByUserOrderByDeadLineDateAscDeadLineTimeAsc(testUser);
        assertThat(list.stream().filter(quest -> quest.getTitle().equals("save title")).findAny().get()).isNotNull();
    }

    @DisplayName("퀘스트 삭제 요청 성공 통합 테스트")
    @Test
    public void testDeleteSuccess() throws Exception {
        //given

        String saveUrl = "http://localhost:" + port + URI_PREFIX + "/save";
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("title", "save title");
        map.add("description", "save description");
        map.add("deadLineDate", "2022-11-11");
        map.add("deadLineTime", "11:11");
        map.add("repeat", "true");
        map.add("difficulty", "easy");
        map.add("rewards", "");

        mvc.perform(
                        post(saveUrl)
                                .with(csrf())
                                .params(map)
                                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                );

        int beforeSize = questRepository.getQuestsByUserOrderByDeadLineDateAscDeadLineTimeAsc(testUser).size();

        List<Quest> list = questRepository.getQuestsByUserOrderByDeadLineDateAscDeadLineTimeAsc(testUser);
        Long questId = list.get(0).getId();

        //when
        String url = "http://localhost:" + port + URI_PREFIX + "/" + questId;

        mvc.perform(delete(url).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/quests"));

        //then
        list = questRepository.getQuestsByUserOrderByDeadLineDateAscDeadLineTimeAsc(testUser);
        assertThat(list.size()).isEqualTo(beforeSize-1);
    }

    @DisplayName("퀘스트 삭제 요청 실패 통합 테스트")
    @Test
    public void testDeleteFail() throws Exception {
        //given
        UserInfo anotherUser = UserInfo.builder()
                .providerType(ProviderType.GOOGLE)
                .difficultyType(DifficultyType.difficulty)
                .oauth2Id("oauth2id")
                .nickname("nickname")
                .build();

        userRepository.save(anotherUser);

        Quest anotherUserQuest = Quest.builder()
                .title("test title")
                .description("test description")
                .deadLineDate(LocalDate.now())
                .state(QuestState.PROCEED)
                .type(QuestType.DAILY)
                .isRepeat(true)
                .difficulty(QuestDifficulty.easy)
                .user(anotherUser)
                .build();

        questRepository.save(anotherUserQuest);

        String url = "http://localhost:" + port + URI_PREFIX + "/" + anotherUserQuest.getId();
        int beforeSize = questRepository.getQuestsByUserOrderByDeadLineDateAscDeadLineTimeAsc(anotherUser).size();

        //when
        mvc.perform(delete(url).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/quests*"))
                .andExpect(model().attributeExists("message"));

        //then
        List<Quest> list = questRepository.getQuestsByUserOrderByDeadLineDateAscDeadLineTimeAsc(anotherUser);
        assertThat(beforeSize).isEqualTo(1);
        assertThat(list.size()).isEqualTo(1);
    }


    @DisplayName("퀘스트 수정 요청 성공 통합 테스트")
    @Test
    public void testUpdateSuccess() throws Exception {
        //given
        UserInfo testUser = UserInfo.builder().id(1L).build();

        String saveUrl = "http://localhost:" + port + URI_PREFIX + "/save";
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("title", "save title");
        map.add("description", "save description");
        map.add("deadLineDate", "2022-11-11");
        map.add("deadLineTime", "11:11");
        map.add("repeat", "true");
        map.add("difficulty", "easy");
        map.add("rewards", "");

        mvc.perform(
                post(saveUrl)
                        .with(csrf())
                        .params(map)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        );

        List<Quest> list = questRepository.getQuestsByUserOrderByDeadLineDateAscDeadLineTimeAsc(testUser);
        Long questId = list.get(0).getId();

        String url = "http://localhost:" + port + URI_PREFIX + "/" + questId;

        map = new LinkedMultiValueMap<>();
        map.add("title", "update title");
        map.add("description", "update description");
        map.add("deadLineDate", "2022-11-11");
        map.add("deadLineTime", "11:11");
        map.add("repeat", "true");
        map.add("reward", "reward1");

        //when
        mvc.perform(
                        put(url)
                            .with(csrf())
                            .params(map)
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/quests/" + questId));

        //then
        Quest findQuest = questRepository.getById(questId);
        assertThat(findQuest.getTitle()).isEqualTo("update title");
    }

    @DisplayName("퀘스트 수정 요청 실패 통합 테스트")
    @Test
    public void testUpdateFail() throws Exception {
        //given
        UserInfo anotherUser = UserInfo.builder()
                .providerType(ProviderType.GOOGLE)
                .difficultyType(DifficultyType.difficulty)
                .oauth2Id("oauth2id")
                .nickname("nickname")
                .build();

        userRepository.save(anotherUser);

        Quest anotherUserQuest = Quest.builder()
                .title("test title")
                .description("test description")
                .deadLineDate(LocalDate.now())
                .state(QuestState.PROCEED)
                .type(QuestType.DAILY)
                .isRepeat(true)
                .difficulty(QuestDifficulty.easy)
                .user(anotherUser)
                .build();

        questRepository.save(anotherUserQuest);

        String url = "http://localhost:" + port + URI_PREFIX + "/" + anotherUserQuest.getId();

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("title", "update title");
        map.add("description", "update description");
        map.add("deadLineDate", "2022-11-11");
        map.add("deadLineTime", "11:11");
        map.add("repeat", "true");
        map.add("reward", "reward1");

        //when
        mvc.perform(
                        put(url)
                                .with(csrf())
                                .params(map)
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/quests*"))
                .andExpect(model().attributeExists("message"));

        //then
        Quest findQuest = questRepository.getById(anotherUserQuest.getId());
        assertThat(findQuest.getTitle()).isNotEqualTo("update title");
    }


}