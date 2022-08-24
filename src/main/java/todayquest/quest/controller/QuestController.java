package todayquest.quest.controller;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import todayquest.quest.dto.QuestRequestDto;
import todayquest.quest.dto.QuestResponseDto;
import todayquest.quest.service.QuestService;
import todayquest.user.dto.UserPrincipal;
import todayquest.user.entity.UserInfo;

import java.security.Principal;
import java.time.LocalDateTime;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/quests")
@Controller
public class QuestController {

    private final QuestService questService;

    @GetMapping("")
    public String list(@AuthenticationPrincipal UserPrincipal principal, Model model) {

        log.info("principal={}", principal);
        log.info(principal.getName());
        log.info("userId = {}", principal.getUserId());

        model.addAttribute("questList", questService.getQuestList(principal.getUserId()));

        return "/quest/list";
    }

    @GetMapping("/save")
    public String saveForm(Model model) {
        model.addAttribute("quest", new QuestRequestDto());
        return "/quest/save";
    }

    @GetMapping("/{questId}")
    public String view(@PathVariable("questId") Long questId, Model model) {
        model.addAttribute("quest", questService.getQuestInfo(questId));
        return "/quest/view";
    }

    @PostMapping("/save")
    public String save(QuestRequestDto dto, @AuthenticationPrincipal UserPrincipal principal) {
        questService.saveQuest(dto, principal);
        return "redirect:/quests";
    }

    @PutMapping("/{questId}")
    public String update(QuestRequestDto dto, @PathVariable("questId") Long questId) {
        questService.updateQuest(dto, questId);
        return "redirect:/quests/{questId}";
    }

    @DeleteMapping("/{questId}")
    public String delete(@PathVariable("questId") Long questId) {
        questService.deleteQuest(questId);
        return "redirect:/quests";
    }


}
