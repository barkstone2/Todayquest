package dailyquest.status.controller;

import dailyquest.common.ResponseData;
import dailyquest.common.TimeUtilKt;
import dailyquest.quest.dto.QuestLogSearchCondition;
import dailyquest.quest.dto.QuestStatisticsResponse;
import dailyquest.quest.service.QuestLogService;
import dailyquest.status.dto.StatisticsResponse;
import dailyquest.user.dto.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Map;
import java.util.function.Function;

@Validated
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/status")
@RestController
public class StatusApiController {

    private final QuestLogService questLogService;

    @GetMapping("/{selectedDate}")
    public ResponseEntity<ResponseData<StatisticsResponse>> getStatistics(
            QuestLogSearchCondition questLogSearchCondition,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Map<LocalDate, QuestStatisticsResponse> questStatistic = questLogService.getQuestStatistic(principal.getId(), questLogSearchCondition);

        LocalDate selectedDate = questLogSearchCondition.getSelectedDate();
        Function<LocalDate, LocalDate> dateKeyTransformFunction =
            switch (questLogSearchCondition.getSearchType()) {
                case WEEKLY -> TimeUtilKt::firstDayOfWeek;
                case MONTHLY -> TimeUtilKt::firstDayOfMonth;
                default -> LocalDate::from;
            };

        StatisticsResponse statisticsResponse = new StatisticsResponse(questStatistic, dateKeyTransformFunction.apply(selectedDate));
        return ResponseEntity.ok(ResponseData.of(statisticsResponse));
    }
}
