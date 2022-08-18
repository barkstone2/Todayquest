package todayquest.quest.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import todayquest.quest.service.QuestService;
import todayquest.user.dto.UserPrincipal;

import java.security.Principal;

@AllArgsConstructor
@RequestMapping("/quest")
@Slf4j
@Controller
public class QuestController {

    private final QuestService questService;
    private static final String VIEW_PREFIX = "/quest";

    @GetMapping("/list")
    public String list(@AuthenticationPrincipal UserPrincipal principal) {

        log.info("principal={}", principal);
        log.info(principal.getName());
        log.info("userId = {}", principal.getUserId());

        return VIEW_PREFIX + "/list";
    }
}
