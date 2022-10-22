package todayquest.user.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import todayquest.quest.service.QuestLogService;
import todayquest.user.dto.UserPrincipal;

import java.io.IOException;
import java.util.Map;

@RequiredArgsConstructor
@RequestMapping("/user")
@Controller
public class UserController {

    private final ResourceLoader resourceLoader;
    private final QuestLogService questLogService;

    @GetMapping("/status")
    public String myPage(@AuthenticationPrincipal UserPrincipal principal, Model model) throws IOException {

        // 경험치 테이블을 읽어온다.
        Resource resource = resourceLoader.getResource("classpath:data/exp_table.json");
        ObjectMapper om = new ObjectMapper();
        Map<Integer, Long> expTable = om.readValue(resource.getInputStream(), new TypeReference<>() {});

        model.addAttribute("targetExp", expTable.get(principal.getLevel()));
        model.addAttribute("questLog", questLogService.getQuestLog(principal.getUserId()));
        return "user/status";
    }

}
