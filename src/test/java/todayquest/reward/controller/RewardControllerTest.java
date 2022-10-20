package todayquest.reward.controller;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
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
import org.springframework.ui.ModelMap;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.WebApplicationContext;
import todayquest.annotation.WithCustomMockUser;
import todayquest.common.MessageUtil;
import todayquest.quest.entity.Quest;
import todayquest.reward.dto.RewardResponseDto;
import todayquest.reward.entity.Reward;
import todayquest.reward.entity.RewardGrade;
import todayquest.reward.repository.RewardRepository;
import todayquest.user.entity.UserInfo;
import todayquest.user.repository.UserRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("보상 아이템 컨트롤러 통합 테스트")
@Slf4j
@WithCustomMockUser(userId = 1L)
@Transactional
@SpringBootTest(webEnvironment = RANDOM_PORT)
public class RewardControllerTest {

    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate restTemplate;

    @Autowired
    RewardRepository rewardRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RewardController rewardController;

    static final String URI_PREFIX = "/rewards";
    static final String SERVER_ADDR = "http://localhost:";


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

    @DisplayName("보상 아이템 목록 요청")
    @Test
    public void testGetList() throws Exception {
        //given
        String url = SERVER_ADDR + port + URI_PREFIX + "/";

        //when
        String body = mvc.perform(get(url))
                .andExpect(status().isOk())
                .andExpect(view().name("/reward/list"))
                .andExpect(model().attributeExists("rewards"))
                .andReturn().getResponse().getContentAsString();

        //then
        assertThat(body).contains("아이템 목록");
    }

    @DisplayName("보상 아이템 조회 요청")
    @Test
    public void testGetReward() throws Exception {
        //given
        String url = SERVER_ADDR + port + URI_PREFIX + "/save";
        MultiValueMap<String, String> param = new LinkedMultiValueMap<>();
        param.add("name", "save name");
        param.add("description", "save description");
        param.add("grade", RewardGrade.E.name());

        //when
        ModelMap modelMap = mvc.perform(post(url)
                        .with(csrf())
                        .params(param)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/rewards*"))
                .andReturn().getModelAndView().getModelMap();
        Long savedId = Long.valueOf(modelMap.get("savedId").toString());

        url = SERVER_ADDR + port + URI_PREFIX + "/" + savedId;

        //when
        ModelMap modelMap1 = mvc.perform(get(url))
                .andExpect(status().isOk())
                .andExpect(view().name("/reward/view"))
                .andReturn().getModelAndView().getModelMap();

        //then
        RewardResponseDto reward = (RewardResponseDto) modelMap1.get("reward");
        assertThat(reward.getName()).isEqualTo("save name");
    }

    @DisplayName("보상 아이템 등록 화면 요청")
    @Test
    public void testGetSaveForm() throws Exception {
        //given
        String url = SERVER_ADDR + port + URI_PREFIX + "/save";

        //when
        //then
        mvc.perform(get(url))
                .andExpect(view().name("/reward/save"))
                .andExpect(model().attributeExists("gradeList"));
    }

    @DisplayName("보상 아이템 등록 통합 테스트_성공")
    @Test
    public void testSave() throws Exception {
        //given
        String url = SERVER_ADDR + port + URI_PREFIX + "/save";
        MultiValueMap<String, String> param = new LinkedMultiValueMap<>();
        param.add("name", "save name");
        param.add("description", "save description");
        param.add("grade", RewardGrade.E.name());

        //when
        ModelMap modelMap = mvc.perform(post(url)
                        .with(csrf())
                        .params(param)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/rewards*"))
                .andReturn().getModelAndView().getModelMap();
        Long savedId = Long.valueOf(modelMap.get("savedId").toString());

        //then
        Optional<Reward> byId = rewardRepository.findById(savedId);
        assertThat(byId).isNotEmpty();
    }


    @DisplayName("보상 아이템 등록 통합 테스트_Validation Fail")
    @Test
    public void testSaveValidationFail() throws Exception {
        //given
        String url = SERVER_ADDR + port + URI_PREFIX + "/save";
        MultiValueMap<String, String> param = new LinkedMultiValueMap<>();
        param.add("name", "save name");
        param.add("description", "");
        param.add("grade", RewardGrade.E.name());

        //when
        //then
        mvc.perform(post(url)
                        .with(csrf())
                        .params(param)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                )
                .andExpect(view().name("/reward/save"))
                .andExpect(model().attributeDoesNotExist("savedId"));
    }

    @DisplayName("보상 아이템 수정 통합 테스트_성공")
    @Test
    public void testUpdate() throws Exception {
        //given
        String saveUrl = "http://localhost:" + port + URI_PREFIX + "/save";
        MultiValueMap<String, String> param = new LinkedMultiValueMap<>();
        param.add("name", "save name");
        param.add("description", "save description");
        param.add("grade", RewardGrade.E.name());

        ModelMap model = mvc.perform(
                post(saveUrl)
                        .with(csrf())
                        .params(param)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        ).andReturn().getModelAndView().getModelMap();

        Long savedId = Long.valueOf(model.get("savedId").toString());
        String beforeName = rewardRepository.getById(savedId).getName();

        String url = "http://localhost:" + port + URI_PREFIX + "/" + savedId;

        param = new LinkedMultiValueMap<>();
        param.add("name", "update name");
        param.add("description", "update description");
        param.add("grade", RewardGrade.E.name());

        //when
        mvc.perform(
                        put(url)
                                .with(csrf())
                                .params(param)
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/rewards/" + savedId + "*"));

        //then
        Reward updateReward = rewardRepository.getById(savedId);
        String afterName = updateReward.getName();
        assertThat(beforeName).isEqualTo("save name");
        assertThat(beforeName).isNotEqualTo(afterName);
        assertThat(afterName).isEqualTo("update name");
    }

    @DisplayName("보상 아이템 수정 통합 테스트_Validation Fail")
    @Test
    public void testUpdateValidationFail() throws Exception {
        //given
        String saveUrl = "http://localhost:" + port + URI_PREFIX + "/save";
        MultiValueMap<String, String> param = new LinkedMultiValueMap<>();
        param.add("name", "save name");
        param.add("description", "save description");
        param.add("grade", RewardGrade.E.name());

        ModelMap model = mvc.perform(
                post(saveUrl)
                        .with(csrf())
                        .params(param)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        ).andReturn().getModelAndView().getModelMap();

        Long savedId = Long.valueOf(model.get("savedId").toString());
        String beforeName = rewardRepository.getById(savedId).getName();

        String url = "http://localhost:" + port + URI_PREFIX + "/" + savedId;

        param = new LinkedMultiValueMap<>();
        param.add("name", "update name");
        param.add("description", "");
        param.add("grade", RewardGrade.E.name());

        //when
        mvc.perform(
                        put(url)
                                .with(csrf())
                                .params(param)
                )
                .andExpect(view().name("/reward/view"))
                .andExpect(model().attributeExists("hasError"));

        //then
        Reward updateReward = rewardRepository.getById(savedId);
        String afterName = updateReward.getName();
        assertThat(beforeName).isEqualTo("save name");
        assertThat(beforeName).isEqualTo(afterName);
    }

    @DisplayName("보상 아이템 수정 통합 테스트_엔티티 정보 없음")
    @Test
    public void testUpdateNotFound() throws Exception {
        //given
        String url = "http://localhost:" + port + URI_PREFIX + "/" + 0L;

        MultiValueMap<String, String> param = new LinkedMultiValueMap<>();
        param.add("name", "update name");
        param.add("description", "update description");
        param.add("grade", RewardGrade.E.name());

        //when
        Exception exception = mvc.perform(
                        put(url)
                                .with(csrf())
                                .params(param)
                )
                .andReturn().getResolvedException();

        //then
        assertThat(exception).isInstanceOf(IllegalArgumentException.class);
        assertThat(exception.getMessage()).isEqualTo(MessageUtil.getMessage("exception.entity.notfound", MessageUtil.getMessage("reward")));
    }

    @DisplayName("보상 아이템 삭제 통합 테스트_성공")
    @Test
    public void testDelete() throws Exception {
        //given
        String saveUrl = SERVER_ADDR + port + URI_PREFIX + "/save";
        MultiValueMap<String, String> param = new LinkedMultiValueMap<>();
        param.add("name", "save name");
        param.add("description", "save description");
        param.add("grade", RewardGrade.E.name());

        ModelMap model = mvc.perform(
                post(saveUrl)
                        .with(csrf())
                        .params(param)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        ).andReturn().getModelAndView().getModelMap();

        Long savedId = Long.valueOf(model.get("savedId").toString());

        Optional<Reward> beforeReward = rewardRepository.findById(savedId);
        assertThat(beforeReward).isNotEmpty();

        String url = SERVER_ADDR + port + URI_PREFIX + "/" + beforeReward.get().getId();

        //when
        mvc.perform(
                        delete(url)
                                .with(csrf())
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/rewards*"));

        //then
        boolean isExist = rewardRepository.existsById(beforeReward.get().getId());
        assertThat(isExist).isFalse();
    }

    @DisplayName("보상 아이템 삭제 통합 테스트_엔티티 정보 없음")
    @Test
    public void testDeleteNotFound() throws Exception {
        //given
        String url = "http://localhost:" + port + URI_PREFIX + "/" + 0L;

        //when
        Exception exception = mvc.perform(
                        delete(url)
                                .with(csrf())
                )
                .andReturn().getResolvedException();

        //then
        assertThat(exception).isInstanceOf(IllegalArgumentException.class);
        assertThat(exception.getMessage()).isEqualTo(MessageUtil.getMessage("exception.entity.notfound", MessageUtil.getMessage("reward")));
    }




}
