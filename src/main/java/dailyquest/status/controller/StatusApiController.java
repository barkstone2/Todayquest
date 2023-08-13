package dailyquest.status.controller;

import dailyquest.common.ResponseData;
import dailyquest.quest.dto.QuestLogSearchCondition;
import dailyquest.quest.dto.QuestStatisticsResponse;
import dailyquest.quest.service.QuestLogService;
import dailyquest.status.dto.StatusResponse;
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

@Validated
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/status")
@RestController
public class StatusApiController {

    private final QuestLogService questLogService;

    @GetMapping("/{selectedDate}")
    public ResponseEntity<ResponseData<StatusResponse>> getStatus(
            QuestLogSearchCondition questLogSearchCondition,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Map<LocalDate, QuestStatisticsResponse> questStatistic = questLogService.getQuestStatistic(principal.getId(), questLogSearchCondition);

        StatusResponse statusResponse = new StatusResponse(questStatistic);

        return ResponseEntity.ok(ResponseData.of(statusResponse));
    }
}
