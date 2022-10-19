package todayquest.quest.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import todayquest.quest.dto.QuestRequestDto;
import todayquest.quest.dto.QuestResponseDto;
import todayquest.quest.entity.QuestDifficulty;
import todayquest.quest.service.QuestService;
import todayquest.reward.service.RewardService;
import todayquest.user.dto.UserPrincipal;

import javax.validation.Valid;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/quests")
@Controller
public class QuestController {

    private final QuestService questService;
    private final RewardService rewardService;


    @GetMapping("")
    public String list(@AuthenticationPrincipal UserPrincipal principal, Model model) {
        model.addAttribute("questList", questService.getQuestList(principal.getUserId()));

        return "/quest/list";
    }

    @GetMapping("/save")
    public String saveForm(Model model, @AuthenticationPrincipal UserPrincipal principal) {
        model.addAttribute("quest", new QuestRequestDto());
        model.addAttribute("difficultyList", QuestDifficulty.getEnumList());
        model.addAttribute("rewardList", rewardService.getRewardList(principal.getUserId()));
        return "/quest/save";
    }

    @GetMapping("/{questId}")
    public String view(@PathVariable("questId") Long questId, Model model, @AuthenticationPrincipal UserPrincipal principal) {
        QuestResponseDto quest = questService.getQuestInfo(questId);
        model.addAttribute("quest", quest);
        model.addAttribute("rewards", quest.getRewards());
        model.addAttribute("difficultyList", QuestDifficulty.getEnumList());
        model.addAttribute("rewardList", rewardService.getRewardList(principal.getUserId()));
        return "/quest/view";
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("quest") QuestRequestDto dto, BindingResult bindingResult, @AuthenticationPrincipal UserPrincipal principal, Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("difficultyList", QuestDifficulty.getEnumList());
            model.addAttribute("rewardList", rewardService.getRewardList(principal.getUserId()));
            return "/quest/save";
        }
        redirectAttributes.addAttribute("savedId", questService.saveQuest(dto, principal.getUserId()));

        return "redirect:/quests";
    }

    @PutMapping("/{questId}")
    public String update(@Valid @ModelAttribute("quest") QuestRequestDto dto, BindingResult bindingResult, @PathVariable("questId") Long questId, @AuthenticationPrincipal UserPrincipal principal, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("rewards", rewardService.getRewardListByIds(dto.getRewards(), principal.getUserId()));
            model.addAttribute("difficultyList", QuestDifficulty.getEnumList());
            model.addAttribute("rewardList", rewardService.getRewardList(principal.getUserId()));
            model.addAttribute("hasError", true);
            return "/quest/view";
        }

        questService.updateQuest(dto, questId, principal.getUserId());

        return "redirect:/quests/{questId}";
    }

    @DeleteMapping("/{questId}")
    public String delete(@PathVariable("questId") Long questId, @AuthenticationPrincipal UserPrincipal principal, RedirectAttributes redirectAttributes) {
        questService.deleteQuest(questId, principal.getUserId());
        return "redirect:/quests";
    }


}
