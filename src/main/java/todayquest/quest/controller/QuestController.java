package todayquest.quest.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import todayquest.quest.dto.QuestRequestDto;
import todayquest.quest.entity.QuestDifficulty;
import todayquest.quest.service.QuestService;
import todayquest.user.dto.UserPrincipal;

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
    public String saveForm(Model model, @AuthenticationPrincipal UserPrincipal principal) {
        model.addAttribute("quest", new QuestRequestDto());
        model.addAttribute("difficultyList", QuestDifficulty.getEnumListOfType(principal.getDifficultyType()));
        return "/quest/save";
    }

    @GetMapping("/{questId}")
    public String view(@PathVariable("questId") Long questId, Model model, @AuthenticationPrincipal UserPrincipal principal) {
        model.addAttribute("quest", questService.getQuestInfo(questId));
        model.addAttribute("difficultyList", QuestDifficulty.getEnumListOfType(principal.getDifficultyType()));
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
