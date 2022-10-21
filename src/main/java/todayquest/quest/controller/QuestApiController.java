package todayquest.quest.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import todayquest.quest.dto.QuestResponseDto;
import todayquest.quest.dto.QuestSearchCondition;
import todayquest.quest.service.QuestService;
import todayquest.user.dto.UserPrincipal;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/quests")
@RestController
public class QuestApiController {
    private final QuestService questService;

    @GetMapping("")
    public ResponseEntity<Slice<QuestResponseDto>> list(QuestSearchCondition condition, @AuthenticationPrincipal UserPrincipal principal) {
        return new ResponseEntity<>(questService.getQuestList(principal.getUserId(), condition.getState(), PageRequest.of(condition.getPage(), 9)), HttpStatus.OK);
    }

}
