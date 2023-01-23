package todayquest.quest.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import todayquest.quest.dto.DetailQuestResponseDto;
import todayquest.quest.dto.QuestResponseDto;
import todayquest.quest.dto.QuestSearchCondition;
import todayquest.quest.service.DetailQuestService;
import todayquest.quest.service.QuestService;
import todayquest.user.dto.UserPrincipal;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/quests")
@RestController
public class QuestApiController {
    private final QuestService questService;
    private final DetailQuestService detailQuestService;

    @GetMapping("")
    public ResponseEntity<Slice<QuestResponseDto>> list(QuestSearchCondition condition, @AuthenticationPrincipal UserPrincipal principal) {
        return new ResponseEntity<>(questService.getQuestList(principal.getUserId(), condition.getState(), PageRequest.of(condition.getPage(), 9)), HttpStatus.OK);
    }

    @PutMapping("/{questId}/details/{detailQuestId}")
    public ResponseEntity<Map<String, Object>> interact(
            @PathVariable("questId") Long questId,
            @PathVariable("detailQuestId") Long detailQuestId,
            @AuthenticationPrincipal UserPrincipal principal) {

        DetailQuestResponseDto interactDetail = detailQuestService.interact(principal.getUserId(), questId, detailQuestId);
        QuestResponseDto parentQuest = questService.getQuestInfo(questId, principal.getUserId());
        boolean canComplete = parentQuest.getCanComplete();

        Map<String, Object> result = new HashMap<>();
        result.put("detailQuest", interactDetail);
        result.put("canComplete", canComplete);

        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
