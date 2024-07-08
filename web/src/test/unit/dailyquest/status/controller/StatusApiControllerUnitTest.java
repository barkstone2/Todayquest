package dailyquest.status.controller;

import dailyquest.annotation.WebMvcUnitTest;
import dailyquest.quest.dto.QuestLogSearchCondition;
import dailyquest.quest.dto.QuestLogSearchType;
import dailyquest.quest.service.QuestLogService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@WebMvcUnitTest(StatusApiController.class)
@DisplayName("상태창 API 컨트롤러 유닛 테스트")
public class StatusApiControllerUnitTest {
    static final String URI_PREFIX = "/api/v1/status";
    
    @Autowired
    MockMvc mvc;
    
    @MockBean
    QuestLogService questLogService;

    @DisplayName("상태창 통계 요청 시 요청 파라미터를 사용해 데이터를 조회한다")
    @Test
    public void testGetStatus() throws Exception {
        //given
        LocalDate selectedDate = LocalDate.of(2022, 12, 12);
        QuestLogSearchType searchType = QuestLogSearchType.WEEKLY;

        String url = URI_PREFIX + "/" + selectedDate + "?searchType=" + searchType;

        ArgumentCaptor<QuestLogSearchCondition> conditionCaptor = ArgumentCaptor.forClass(QuestLogSearchCondition.class);

        //when
        ResultActions result = mvc.perform(get(url));

        //then
        verify(questLogService).getQuestStatistic(any(), conditionCaptor.capture());
        QuestLogSearchCondition requestCondition = conditionCaptor.getValue();
        assertThat(requestCondition.getSearchType()).isEqualTo(searchType);
        assertThat(requestCondition.getSelectedDate()).isEqualTo(selectedDate);
        result.andExpect(status().isOk());
    }

    @DisplayName("상태창 통계 요청 시 search type 파라미터가 비어있다면 기본값을 사용한다")
    @Test
    public void testIfSelectedDateIsEmpty() throws Exception {
        //given
        LocalDate selectedDate = LocalDate.of(2022, 12, 12);
        String url = URI_PREFIX + "/" + selectedDate;

        ArgumentCaptor<QuestLogSearchCondition> conditionCaptor = ArgumentCaptor.forClass(QuestLogSearchCondition.class);
        QuestLogSearchType defaultSearchType = new QuestLogSearchCondition().getSearchType();

        //when
        ResultActions result = mvc.perform(get(url));

        //then
        verify(questLogService).getQuestStatistic(any(), conditionCaptor.capture());
        QuestLogSearchCondition requestCondition = conditionCaptor.getValue();
        assertThat(requestCondition.getSelectedDate()).isEqualTo(selectedDate);
        assertThat(requestCondition.getSearchType()).isEqualTo(defaultSearchType);
        result.andExpect(status().isOk());
    }

}
