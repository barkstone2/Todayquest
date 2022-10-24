package todayquest.quest.controller;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.data.domain.Slice;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;
import todayquest.annotation.WithCustomMockUser;
import todayquest.common.DatabaseCleanup;
import todayquest.common.MessageUtil;
import todayquest.quest.entity.Quest;
import todayquest.quest.entity.QuestDifficulty;
import todayquest.quest.entity.QuestState;
import todayquest.quest.entity.QuestType;
import todayquest.quest.repository.QuestRepository;
import todayquest.user.entity.ProviderType;
import todayquest.user.entity.UserInfo;
import todayquest.user.repository.UserRepository;

import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
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
    static final String URI_PREFIX = "/quests";

    @BeforeEach
    public void setUp() {
        databaseCleanup.execute();
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .addFilter(new CharacterEncodingFilter("UTF-8", true))
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

    @DisplayName("퀘스트 목록 화면 요청 통합 테스트")
    @Test
    public void testList() throws Exception {
        //given
        String url = SERVER_ADDR + port + URI_PREFIX + "/";

        //when
        String body = mvc.perform(get(url))
                .andExpect(status().isOk())
                .andExpect(view().name("quest/list"))
                .andExpect(model().attributeExists("questList"))
                .andReturn().getResponse().getContentAsString();

        //then
        assertThat(body).contains("퀘스트 목록");
    }

    @DisplayName("퀘스트 목록 화면 요청_검색조건 통합 테스트")
    @Test
    public void testListWithCondition() throws Exception {
        //given
        String url = SERVER_ADDR + port + URI_PREFIX + "/";

        MultiValueMap<String, String> cond = new LinkedMultiValueMap<>();
        cond.add("state", QuestState.COMPLETE.name());

        //when
        Map<String, Object> model = mvc.perform(get(url)
                        .params(cond))
                .andExpect(status().isOk())
                .andExpect(view().name("quest/list"))
                .andExpect(model().attributeExists("questList"))
                .andReturn().getModelAndView().getModel();

        Slice questList = (Slice) model.get("questList");

        //then
        assertThat(questList.getContent().size()).isEqualTo(0);
    }


    @DisplayName("퀘스트 등록 화면 요청 통합 테스트")
    @Test
    public void testSaveForm() throws Exception {
        //given
        String url = SERVER_ADDR + port + URI_PREFIX + "/save";

        //when
        String body = mvc.perform(get(url))
                .andExpect(status().isOk())
                .andExpect(view().name("quest/save"))
                .andExpect(model().attributeExists("quest", "difficultyList", "rewardList"))
                .andReturn().getResponse().getContentAsString();

        //then
        assertThat(body).contains("퀘스트 등록");
    }



    @DisplayName("퀘스트 상세보기 화면 요청 통합 테스트")
    @Test
    public void testView() throws Exception {
        //given
        String url = SERVER_ADDR + port + URI_PREFIX + "/" + testQuest.getId();

        //when
        String body = mvc.perform(get(url))
                .andExpect(status().isOk())
                .andExpect(view().name("quest/view"))
                .andExpect(model().attributeExists("quest", "rewards"))
                .andReturn().getResponse().getContentAsString();

        //then
        assertThat(body).contains("퀘스트 조회");
    }


    @DisplayName("퀘스트 등록 통합 테스트_성공")
    @Test
    public void testSave() throws Exception {
        //given
        String url = SERVER_ADDR + port + URI_PREFIX + "/save";

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("title", "save title");
        map.add("description", "save description");
        map.add("repeat", "true");
        map.add("difficulty", QuestDifficulty.easy.name());

        //when
        //then
        mvc.perform(
                        post(url)
                                .with(csrf())
                                .params(map)
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/quests*"))
                .andReturn().getModelAndView().getModelMap();

    }

    @DisplayName("퀘스트 등록 통합 테스트_Validation 실패")
    @Test
    public void testSaveValidationFail() throws Exception {
        //given
        String url = SERVER_ADDR + port + URI_PREFIX + "/save";

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
                .andExpect(view().name("quest/save"));
    }

    @DisplayName("퀘스트 수정 통합 테스트_성공")
    @Test
    public void testUpdateSuccess() throws Exception {
        //given
        Quest findQuest = questRepository.getById(testQuest.getId());
        String beforeTitle = findQuest.getTitle();

        Long questId = findQuest.getId();
        String url = SERVER_ADDR + port + URI_PREFIX + "/" + questId;

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("title", beforeTitle + "update");
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
                .andExpect(redirectedUrl("/quests/" + questId));

        //then
        findQuest = questRepository.getById(questId);
        String afterTitle = findQuest.getTitle();

        assertThat(beforeTitle).isNotEqualTo(afterTitle);
        assertThat(afterTitle).isEqualTo(beforeTitle + "update");
    }


    @DisplayName("퀘스트 수정 통합 테스트_Validation 실패")
    @Test
    public void testUpdateFailValidation() throws Exception {
        //given
        Quest findQuest = questRepository.getById(testQuest.getId());
        String beforeTitle = findQuest.getTitle();

        Long questId = findQuest.getId();
        String url = SERVER_ADDR + port + URI_PREFIX + "/" + questId;

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("title", beforeTitle + "update");
        map.add("description", "");
        map.add("repeat", "true");
        map.add("reward", "reward1");
        map.add("state", QuestState.PROCEED.name());
        map.add("difficulty", QuestDifficulty.easy.name());

        //when
        mvc.perform(
                        put(url)
                                .with(csrf())
                                .params(map)
                )
                .andExpect(view().name("quest/view"))
                .andExpect(model().attributeExists("hasError"))
                .andReturn().getResolvedException();

        //then
        findQuest = questRepository.getById(questId);
        String afterTitle = findQuest.getTitle();

        assertThat(beforeTitle).isEqualTo(afterTitle);
        assertThat(beforeTitle+"update").isNotEqualTo(afterTitle);
    }

    @DisplayName("퀘스트 수정 통합 테스트_다른 유저의 퀘스트")
    @Test
    public void testUpdateAnotherUser() throws Exception {
        //given
        UserInfo anotherUser = UserInfo.builder()
                .providerType(ProviderType.GOOGLE)
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
                .seq(1L)
                .build();

        questRepository.save(anotherUserQuest);

        String beforeTitle = anotherUserQuest.getTitle();

        String url = SERVER_ADDR + port + URI_PREFIX + "/" + anotherUserQuest.getId();

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
        Quest findQuest = questRepository.getById(testQuest.getId());
        String beforeTitle = findQuest.getTitle();

        String url = SERVER_ADDR + port + URI_PREFIX + "/" + 0L;

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
        findQuest = questRepository.getById(testQuest.getId());
        String afterTitle = findQuest.getTitle();
        assertThat(beforeTitle).isEqualTo(afterTitle);
        assertThat(beforeTitle + "update").isNotEqualTo(afterTitle);
        assertThat(exception).isInstanceOf(IllegalArgumentException.class);
        assertThat(exception.getMessage()).isEqualTo(MessageUtil.getMessage("exception.entity.notfound", MessageUtil.getMessage("quest")));
    }

    @DisplayName("퀘스트 삭제 통합 테스트")
    @Test
    public void testDeleteSuccess() throws Exception {
        //given
        Long questId = testQuest.getId();
        QuestState beforeState = testQuest.getState();

        //when
        String url = SERVER_ADDR + port + URI_PREFIX + "/" + questId;

        mvc.perform(delete(url).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/quests"));

        //then
        Quest findQuest = questRepository.getById(questId);
        assertThat(beforeState).isNotEqualTo(QuestState.DELETE);
        assertThat(findQuest.getId()).isEqualTo(testQuest.getId());
        assertThat(findQuest.getState()).isEqualTo(QuestState.DELETE);
    }

    @DisplayName("퀘스트 삭제 통합 테스트_다른 유저의 퀘스트")
    @Test
    public void testDeleteAnotherUser() throws Exception {
        //given
        UserInfo anotherUser = UserInfo.builder()
                .providerType(ProviderType.GOOGLE)
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
                .seq(1L)
                .build();

        questRepository.save(anotherUserQuest);

        String url = SERVER_ADDR + port + URI_PREFIX + "/" + anotherUserQuest.getId();

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
        String url = SERVER_ADDR + port + URI_PREFIX + "/" + 0L;

        //when
        Exception exception = mvc.perform(delete(url).with(csrf()))
                .andReturn().getResolvedException();

        //then
        assertThat(exception).isInstanceOf(IllegalArgumentException.class);
        assertThat(exception.getMessage()).isEqualTo(MessageUtil.getMessage("exception.entity.notfound", MessageUtil.getMessage("quest")));
    }


    @DisplayName("퀘스트 완료 통합 테스트")
    @Test
    public void testComplete() throws Exception {
        //given
        Long questId = testQuest.getId();
        QuestState beforeState = testQuest.getState();
        String url = SERVER_ADDR + port + URI_PREFIX + "/" + questId;
        Long beforeExp = testUser.getExp();
        Long beforeGold = testUser.getGold();

        //when
        mvc.perform(post(url).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/quests"));

        //then
        Quest findQuest = questRepository.getById(questId);

        assertThat(beforeState).isNotEqualTo(QuestState.COMPLETE);
        assertThat(findQuest.getState()).isEqualTo(QuestState.COMPLETE);
        assertThat(testUser.getExp()).isEqualTo(beforeExp + findQuest.getDifficulty().getExperience());
        assertThat(testUser.getGold()).isEqualTo(beforeGold + findQuest.getDifficulty().getGold());
    }



    @DisplayName("퀘스트 포기 통합 테스트")
    @Test
    public void testDiscard() throws Exception {
        //given
        Long questId = testQuest.getId();
        QuestState beforeState = testQuest.getState();
        String url = SERVER_ADDR + port + URI_PREFIX + "/" + questId + "/discard";

        //when
        mvc.perform(delete(url).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/quests"));

        //then
        Quest findQuest = questRepository.getById(questId);

        assertThat(beforeState).isNotEqualTo(QuestState.DISCARD);
        assertThat(findQuest.getState()).isEqualTo(QuestState.DISCARD);
    }






}