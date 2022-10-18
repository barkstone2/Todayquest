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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.WebApplicationContext;
import todayquest.annotation.WithCustomMockUser;
import todayquest.common.MessageUtil;
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
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Slf4j
@DisplayName("퀘스트 컨트롤러 통합 테스트")
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


    @DisplayName("퀘스트 등록 통합 테스트_성공")
    @Test
    public void testSave() throws Exception {
        //given
        String url = "http://localhost:" + port + URI_PREFIX + "/save";

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("title", "save title");
        map.add("description", "save description");
        map.add("repeat", "true");
        map.add("difficulty", QuestDifficulty.easy.name());

        //when
        //then
        ModelMap modelMap = mvc.perform(
                        post(url)
                                .with(csrf())
                                .params(map)
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/quests*"))
                .andReturn().getModelAndView().getModelMap();
        Long savedId = Long.valueOf(modelMap.get("savedId").toString());

        String savedTitle = questRepository.getById(savedId).getTitle();
        assertThat(savedTitle).isEqualTo("save title");
    }

    @DisplayName("퀘스트 등록 통합 테스트_Validation 실패")
    @Test
    public void testSaveValidationFail() throws Exception {
        //given
        String url = "http://localhost:" + port + URI_PREFIX + "/save";

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("title", "save title");
        map.add("description", "");
        map.add("repeat", "true");
        map.add("difficulty", QuestDifficulty.easy.name());

        //when
        //then
        mvc.perform(
                        post(url)
                                .with(csrf())
                                .params(map)
                                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                )
                .andExpect(view().name("/quest/save"));
    }


    @DisplayName("퀘스트 삭제 통합 테스트")
    @Test
    public void testDeleteSuccess() throws Exception {
        //given

        String saveUrl = "http://localhost:" + port + URI_PREFIX + "/save";
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("title", "save title");
        map.add("description", "save description");
        map.add("repeat", "true");
        map.add("difficulty", QuestDifficulty.easy.name());

        ModelMap modelMap = mvc.perform(
                post(saveUrl)
                        .with(csrf())
                        .params(map)
        ).andReturn().getModelAndView().getModelMap();
        Long savedId = Long.valueOf(modelMap.get("savedId").toString());

        String savedTitle = questRepository.getById(savedId).getTitle();

        //when
        String url = "http://localhost:" + port + URI_PREFIX + "/" + savedId;

        mvc.perform(delete(url).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/quests"));

        //then
        Optional<Quest> findQuest = questRepository.findById(savedId);
        assertThat(savedTitle).isEqualTo("save title");
        assertThat(findQuest).isEmpty();
    }

    @DisplayName("퀘스트 삭제 통합 테스트_다른 유저의 퀘스트")
    @Test
    public void testDeleteAnotherUser() throws Exception {
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

        //when
        Exception exception = mvc.perform(delete(url).with(csrf()))
                .andReturn().getResolvedException();

        //then
        Quest findQuest = questRepository.getById(anotherUserQuest.getId());
        assertThat(exception).isInstanceOf(AccessDeniedException.class);
        assertThat(exception.getMessage()).isEqualTo(MessageUtil.getMessage("exception.access.denied", MessageUtil.getMessage("quest")));
        assertThat(findQuest).isNotNull();
    }

    @DisplayName("퀘스트 삭제 통합 테스트_퀘스트 정보 없음")
    @Test
    public void testDeleteNotFound() throws Exception {
        //given
        String url = "http://localhost:" + port + URI_PREFIX + "/" + 0L;

        //when
        Exception exception = mvc.perform(delete(url).with(csrf()))
                .andReturn().getResolvedException();

        //then
        assertThat(exception).isInstanceOf(IllegalArgumentException.class);
        assertThat(exception.getMessage()).isEqualTo(MessageUtil.getMessage("exception.entity.notfound", MessageUtil.getMessage("quest")));
    }


    @DisplayName("퀘스트 수정 통합 테스트_성공")
    @Test
    public void testUpdateSuccess() throws Exception {
        //given
        UserInfo testUser = UserInfo.builder().id(1L).build();

        String saveUrl = "http://localhost:" + port + URI_PREFIX + "/save";
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("title", "save title");
        map.add("description", "save description");
        map.add("repeat", "true");
        map.add("difficulty", QuestDifficulty.easy.name());

        ModelMap modelMap = mvc.perform(
                post(saveUrl)
                        .with(csrf())
                        .params(map)
        ).andReturn().getModelAndView().getModelMap();
        Long savedId = Long.valueOf(modelMap.get("savedId").toString());

        Quest findQuest = questRepository.getById(savedId);
        String beforeTitle = findQuest.getTitle();

        String url = "http://localhost:" + port + URI_PREFIX + "/" + savedId;

        map = new LinkedMultiValueMap<>();
        map.add("title", "update title");
        map.add("description", "update description");
        map.add("repeat", "true");
        map.add("reward", "reward1");
        map.add("difficulty", QuestDifficulty.easy.name());

        //when
        mvc.perform(
                        put(url)
                            .with(csrf())
                            .params(map)
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/quests/" + savedId));

        //then
        findQuest = questRepository.getById(savedId);
        String afterTitle = findQuest.getTitle();
        assertThat(beforeTitle).isNotEqualTo(afterTitle);
        assertThat(afterTitle).isEqualTo("update title");
    }


    @DisplayName("퀘스트 수정 통합 테스트_Validation 실패")
    @Test
    public void testUpdateFailValidation() throws Exception {
        //given
        UserInfo testUser = UserInfo.builder().id(1L).build();

        String saveUrl = "http://localhost:" + port + URI_PREFIX + "/save";
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("title", "save title");
        map.add("description", "save description");
        map.add("repeat", "true");
        map.add("difficulty", QuestDifficulty.easy.name());

        ModelMap modelMap = mvc.perform(
                post(saveUrl)
                        .with(csrf())
                        .params(map)
        ).andReturn().getModelAndView().getModelMap();
        Long savedId = Long.valueOf(modelMap.get("savedId").toString());

        Quest findQuest = questRepository.getById(savedId);
        String beforeTitle = findQuest.getTitle();

        String url = "http://localhost:" + port + URI_PREFIX + "/" + savedId;

        map = new LinkedMultiValueMap<>();
        map.add("title", beforeTitle + "111");
        map.add("description", "");
        map.add("repeat", "true");
        map.add("difficulty", QuestDifficulty.easy.name());

        //when
        mvc.perform(
                        put(url)
                                .with(csrf())
                                .params(map)
                )
                .andExpect(view().name("/quest/view"))
                .andExpect(model().attributeExists("hasError"))
                .andReturn().getResolvedException();

        //then
        findQuest = questRepository.getById(savedId);
        String afterTitle = findQuest.getTitle();

        assertThat(beforeTitle).isEqualTo(afterTitle);
        assertThat(beforeTitle+"111").isNotEqualTo(afterTitle);
    }

    @DisplayName("퀘스트 수정 통합 테스트_다른 유저의 퀘스트")
    @Test
    public void testUpdateAnotherUser() throws Exception {
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

        String beforeTitle = anotherUserQuest.getTitle();

        String url = "http://localhost:" + port + URI_PREFIX + "/" + anotherUserQuest.getId();

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("title", beforeTitle + "update");
        map.add("description", "update description");
        map.add("repeat", "true");
        map.add("difficulty", QuestDifficulty.easy.name());

        //when
        Exception exception = mvc.perform(
                        put(url)
                                .with(csrf())
                                .params(map)
                )
                .andReturn().getResolvedException();

        //then
        anotherUserQuest = questRepository.getById(anotherUserQuest.getId());
        String afterTitle = anotherUserQuest.getTitle();
        assertThat(beforeTitle).isEqualTo(afterTitle);
        assertThat(beforeTitle+"update").isNotEqualTo(afterTitle);
        assertThat(exception).isInstanceOf(AccessDeniedException.class);
        assertThat(exception.getMessage()).isEqualTo(MessageUtil.getMessage("exception.access.denied", MessageUtil.getMessage("quest")));
    }

    @DisplayName("퀘스트 수정 통합 테스트_퀘스트 정보 없음")
    @Test
    public void testUpdateNotFound() throws Exception {
        //given
        UserInfo testUser = UserInfo.builder().id(1L).build();

        String saveUrl = "http://localhost:" + port + URI_PREFIX + "/save";
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("title", "save title");
        map.add("description", "save description");
        map.add("repeat", "true");
        map.add("difficulty", QuestDifficulty.easy.name());

        ModelMap modelMap = mvc.perform(
                post(saveUrl)
                        .with(csrf())
                        .params(map)
        ).andReturn().getModelAndView().getModelMap();
        Long savedId = Long.valueOf(modelMap.get("savedId").toString());

        Quest findQuest = questRepository.getById(savedId);
        String beforeTitle = findQuest.getTitle();

        String url = "http://localhost:" + port + URI_PREFIX + "/" + 0L;

        map = new LinkedMultiValueMap<>();
        map.add("title", beforeTitle + "update");
        map.add("description", "update description");
        map.add("repeat", "true");
        map.add("difficulty", QuestDifficulty.easy.name());

        //when
        Exception exception = mvc.perform(
                        put(url)
                                .with(csrf())
                                .params(map)
                )
                .andReturn().getResolvedException();

        //then
        findQuest = questRepository.getById(savedId);
        String afterTitle = findQuest.getTitle();
        assertThat(beforeTitle).isEqualTo(afterTitle);
        assertThat(beforeTitle + "update").isNotEqualTo(afterTitle);
        assertThat(exception).isInstanceOf(IllegalArgumentException.class);
        assertThat(exception.getMessage()).isEqualTo(MessageUtil.getMessage("exception.entity.notfound", MessageUtil.getMessage("quest")));
    }


}