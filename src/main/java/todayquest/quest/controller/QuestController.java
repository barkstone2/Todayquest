package todayquest.quest.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import todayquest.quest.dto.QuestRequestDto;
import todayquest.quest.entity.QuestDifficulty;
import todayquest.quest.service.QuestService;
import todayquest.reward.service.RewardService;
import todayquest.user.dto.UserPrincipal;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/quests")
@Controller
public class QuestController {

    private final QuestService questService;
    private final RewardService rewardService;

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
        model.addAttribute("rewardList", rewardService.getRewardList(principal));
        return "/quest/save";
    }

    @GetMapping("/{questId}")
    public String view(@PathVariable("questId") Long questId, Model model, @AuthenticationPrincipal UserPrincipal principal) {
        model.addAttribute("quest", questService.getQuestInfo(questId));
        model.addAttribute("difficultyList", QuestDifficulty.getEnumListOfType(principal.getDifficultyType()));
        model.addAttribute("rewardList", rewardService.getRewardList(principal));
        return "/quest/view";
    }

    @PostMapping("/save")
    public String save(QuestRequestDto dto, @AuthenticationPrincipal UserPrincipal principal) {
        questService.saveQuest(dto, principal);
        return "redirect:/quests";
    }

    @PutMapping("/{questId}")
    public String update(QuestRequestDto dto, @PathVariable("questId") Long questId, @AuthenticationPrincipal UserPrincipal principal, RedirectAttributes redirectAttributes) {
        boolean isUpdated = questService.updateQuest(dto, questId, principal.getUserId());
        if (!isUpdated) {
            redirectAttributes.addAttribute("message", "비정상적인 접근입니다.");
            return "redirect:/quests";
        }

        return "redirect:/quests/{questId}";
    }

    @DeleteMapping("/{questId}")
    public String delete(@PathVariable("questId") Long questId, @AuthenticationPrincipal UserPrincipal principal, RedirectAttributes redirectAttributes) {
        boolean isDeleted = questService.deleteQuest(questId, principal.getUserId());
        if (!isDeleted) {
            redirectAttributes.addAttribute("message", "비정상적인 접근입니다.");
        }

        return "redirect:/quests";
    }


}
