package dailyquest.status.controller;

import com.jayway.jsonpath.JsonPath;
import dailyquest.context.IntegrationTestContext;
import dailyquest.context.MockElasticsearchTestContextConfig;
import dailyquest.context.MockRedisTestContextConfig;
import dailyquest.quest.dto.QuestLogSearchType;
import dailyquest.quest.entity.QuestLog;
import dailyquest.quest.entity.QuestState;
import dailyquest.quest.entity.QuestType;
import dailyquest.quest.repository.QuestLogRepository;
import dailyquest.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import({MockRedisTestContextConfig.class, MockElasticsearchTestContextConfig.class})
@DisplayName("상태창 API 컨트롤러 통합 테스트")
public class StatusApiControllerTest extends IntegrationTestContext {

    static final String URI_PREFIX = "/api/v1/status";

    @Autowired
    UserService userService;

    @Autowired
    QuestLogRepository questLogRepository;

    @DisplayName("상태창 조회 요청 시")
    @Nested
    class TestGetStatus {

        @DisplayName("요청된 날짜에 대한 범위의 로그가 조회된다.")
        @Test
        public void testGetStatusWhenExist() throws Exception {
            //given
            LocalDate loggedDate = LocalDate.of(2012, 12, 12);
            QuestLog log = QuestLog.builder()
                    .questId(1L)
                    .state(QuestState.COMPLETE)
                    .userId(user.getId())
                    .type(QuestType.MAIN)
                    .loggedDate(loggedDate)
                    .build();

            String url = SERVER_ADDR + getPort() + URI_PREFIX + "/" + loggedDate;
            QuestLogSearchType searchType = QuestLogSearchType.DAILY;

            questLogRepository.save(log);

            //when
            ResultActions request = mvc
                    .perform(
                            get(url)
                                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                                    .with(csrf())
                                    .cookie(userToken)
                                    .queryParam("searchType", searchType.name())
                    );

            //then
            request
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                    .andExpect(jsonPath("$.data").exists())
                    .andExpect(jsonPath("$.data.questStatistics").exists())
                    .andExpect(jsonPath("$.data.questStatistics." + loggedDate).exists())
                    .andExpect(jsonPath("$.data.questStatistics." + loggedDate + ".completeCount").value(1));
        }

        @DisplayName("요청된 날짜에 대한 범위를 벗어난 로그는 조회되지 않는다.")
        @Test
        public void testGetStatusWhenNotExist() throws Exception {
            //given
            LocalDate loggedDate = LocalDate.of(2012, 12, 12);
            QuestLog log = QuestLog.builder()
                    .questId(1L)
                    .state(QuestState.COMPLETE)
                    .userId(user.getId())
                    .type(QuestType.MAIN)
                    .loggedDate(loggedDate)
                    .build();
            LocalDate selectedDate = loggedDate.plusDays(1);
            String url = SERVER_ADDR + getPort() + URI_PREFIX + "/" + selectedDate;
            QuestLogSearchType searchType = QuestLogSearchType.DAILY;


            questLogRepository.save(log);

            //when
            ResultActions request = mvc
                    .perform(
                            get(url)
                                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                                    .with(csrf())
                                    .cookie(userToken)
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
            String url = SERVER_ADDR + getPort() + URI_PREFIX + "/" + selectedDate;

            //when
            ResultActions request = mvc
                    .perform(
                            get(url)
                                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                                    .with(csrf())
                                    .cookie(userToken)
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
