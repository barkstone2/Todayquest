package dailyquest.status.controller;

import com.jayway.jsonpath.JsonPath;
import dailyquest.context.IntegrationTestContextBaseConfig;
import dailyquest.context.MockRedisTestContextConfig;
import dailyquest.jwt.JwtTokenProvider;
import dailyquest.quest.dto.QuestLogSearchType;
import dailyquest.quest.entity.QuestLog;
import dailyquest.quest.entity.QuestState;
import dailyquest.quest.entity.QuestType;
import dailyquest.quest.repository.QuestLogRepository;
import dailyquest.quest.service.QuestLogService;
import dailyquest.status.controller.StatusApiControllerTest.StatusControllerIntegrationTestConfig;
import dailyquest.user.dto.UserPrincipal;
import dailyquest.user.entity.ProviderType;
import dailyquest.user.repository.UserRepository;
import dailyquest.user.service.UserService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("상태창 API 컨트롤러 통합 테스트")
@SpringBootTest(
        webEnvironment = WebEnvironment.RANDOM_PORT,
        classes = {StatusControllerIntegrationTestConfig.class}
)
public class StatusApiControllerTest {

    @Import({
            IntegrationTestContextBaseConfig.class,
            MockRedisTestContextConfig.class,
            QuestLogService.class,
        }
    )
    @ComponentScan(basePackages = "dailyquest.status")
    @EnableJpaRepositories(
            basePackageClasses = {QuestLogRepository.class},
            includeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {QuestLogRepository.class})})
    @EntityScan(basePackageClasses = {QuestLog.class})
    static class StatusControllerIntegrationTestConfig { }
    
    static final String SERVER_ADDR = "http://localhost:";
    static final String URI_PREFIX = "/api/v1/status";

    @LocalServerPort
    int port = 0;

    @Autowired
    JwtTokenProvider jwtTokenProvider;

    @Autowired
    WebApplicationContext context;

    @Autowired
    UserService userService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    QuestLogRepository questLogRepository;

    MockMvc mvc;
    UserPrincipal testUser;
    Cookie token;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .addFilter(new CharacterEncodingFilter("UTF-8", true))
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

        testUser = userService.getOrRegisterUser("user1", ProviderType.GOOGLE);

        String accessToken = jwtTokenProvider.createAccessToken(testUser.getId());
        token = jwtTokenProvider.createAccessTokenCookie(accessToken);
    }

    @DisplayName("상태창 조회 요청 시")
    @Nested
    class TestGetStatus {

        @DisplayName("요청된 날짜에 대한 범위의 로그가 조회된다.")
        @Test
        public void testGetStatusWhenExist() throws Exception {
            //given
            QuestLog log = QuestLog.builder()
                    .questId(1L)
                    .state(QuestState.COMPLETE)
                    .userId(testUser.getId())
                    .type(QuestType.MAIN)
                    .build();

            LocalDate selectedDate = log.getLoggedDate();
            String url = SERVER_ADDR + port + URI_PREFIX + "/" + selectedDate;
            QuestLogSearchType searchType = QuestLogSearchType.DAILY;

            questLogRepository.save(log);

            //when
            ResultActions request = mvc
                    .perform(
                            get(url)
                                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                                    .with(csrf())
                                    .cookie(token)
                                    .queryParam("searchType", searchType.name())
                    );

            //then
            request
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                    .andExpect(jsonPath("$.data").exists())
                    .andExpect(jsonPath("$.data.questStatistics").exists())
                    .andExpect(jsonPath("$.data.questStatistics." + selectedDate).exists())
                    .andExpect(jsonPath("$.data.questStatistics." + selectedDate + ".completeCount").value(1));
        }

        @DisplayName("요청된 날짜에 대한 범위를 벗어난 로그는 조회되지 않는다.")
        @Test
        public void testGetStatusWhenNotExist() throws Exception {
            //given
            QuestLog log = QuestLog.builder()
                    .questId(1L)
                    .state(QuestState.COMPLETE)
                    .userId(testUser.getId())
                    .type(QuestType.MAIN)
                    .build();
            LocalDate selectedDate = log.getLoggedDate().plusDays(1);
            String url = SERVER_ADDR + port + URI_PREFIX + "/" + selectedDate;
            QuestLogSearchType searchType = QuestLogSearchType.DAILY;


            questLogRepository.save(log);

            //when
            ResultActions request = mvc
                    .perform(
                            get(url)
                                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                                    .with(csrf())
                                    .cookie(token)
                                    .queryParam("searchType", searchType.name())
                    );

            //then
            request
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                    .andExpect(jsonPath("$.data").exists())
                    .andExpect(jsonPath("$.data.questStatistics").exists())
                    .andExpect(jsonPath("$.data.questStatistics." + selectedDate).exists())
                    .andExpect(jsonPath("$.data.questStatistics." + selectedDate + ".completeCount").value(0));
        }

        @DisplayName("요청 타입에 따라 조회된 데이터의 간격이 달라진다.")
        @ValueSource(strings = {"DAILY", "WEEKLY", "MONTHLY"})
        @ParameterizedTest
        public void testGetStatusAboutType(QuestLogSearchType searchType) throws Exception {
            //given
            LocalDate selectedDate = LocalDate.now();
            String url = SERVER_ADDR + port + URI_PREFIX + "/" + selectedDate;

            //when
            ResultActions request = mvc
                    .perform(
                            get(url)
                                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                                    .with(csrf())
                                    .cookie(token)
                                    .queryParam("searchType", searchType.name())
                    );

            //then
            MvcResult mvcResult = request
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                    .andExpect(jsonPath("$.data").exists())
                    .andExpect(jsonPath("$.data.questStatistics").exists())
                    .andReturn();

            String responseString = mvcResult.getResponse().getContentAsString();
            Set<String> dateKeys = JsonPath.read(responseString, "$.data.questStatistics.keys()");
            List<LocalDate> dates = dateKeys.stream().map(LocalDate::parse).toList();

            LocalDate start = dates.get(0);

            for (int i = 1; i < dates.size(); i++) {
                switch (searchType) {
                    case DAILY -> assertThat(start.plusDays(i)).isEqualTo(dates.get(i));
                    case WEEKLY -> assertThat(start.plusWeeks(i)).isEqualTo(dates.get(i));
                    case MONTHLY -> assertThat(start.plusMonths(i)).isEqualTo(dates.get(i));
                }
            }
        }

    }



}
