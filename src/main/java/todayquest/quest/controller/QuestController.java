package todayquest.quest.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import todayquest.common.UserLevelLock;
import todayquest.quest.dto.QuestRequestDto;
import todayquest.quest.dto.QuestResponseDto;
import todayquest.quest.dto.QuestSearchCondition;
import todayquest.quest.entity.QuestDifficulty;
import todayquest.quest.entity.QuestState;
import todayquest.quest.service.QuestService;
import todayquest.reward.service.RewardService;
import todayquest.user.dto.UserPrincipal;

import javax.validation.Valid;
import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/quests")
@Controller
public class QuestController {

    private final QuestService questService;
    private final RewardService rewardService;
    private final UserLevelLock userLevelLock;

    @GetMapping("")
    public String list(@ModelAttribute("searchCondition") QuestSearchCondition condition, @AuthenticationPrincipal UserPrincipal principal, Model model) {
        model.addAttribute("questList", questService.getQuestList(principal.getUserId(), condition.getState(), PageRequest.of(0, 9)));
        model.addAttribute("stateList", QuestState.getEnumListForUser());
        return "quest/list";
    }

    @GetMapping("/save")
    public String saveForm(Model model, @AuthenticationPrincipal UserPrincipal principal) {
        model.addAttribute("quest", new QuestRequestDto());
        model.addAttribute("difficultyList", QuestDifficulty.getEnumList());
        model.addAttribute("rewardList", rewardService.getRewardList(principal.getUserId()));
        return "quest/save";
    }

    @GetMapping("/{questId}")
    public String view(@PathVariable("questId") Long questId, Model model, @AuthenticationPrincipal UserPrincipal principal) {
        QuestResponseDto quest = questService.getQuestInfo(questId, principal.getUserId());
        model.addAttribute("quest", quest);
        model.addAttribute("rewards", quest.getRewards());
        model.addAttribute("difficultyList", QuestDifficulty.getEnumList());
        model.addAttribute("rewardList", rewardService.getRewardList(principal.getUserId()));
        return "quest/view";
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("quest") QuestRequestDto dto, BindingResult bindingResult, @AuthenticationPrincipal UserPrincipal principal, Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("difficultyList", QuestDifficulty.getEnumList());
            model.addAttribute("rewardList", rewardService.getRewardList(principal.getUserId()));
            return "quest/save";
        }

        userLevelLock.executeWithLock(
                "QUEST_SEQ" + principal.getUserId(),
                3,
                () -> questService.saveQuest(dto, principal.getUserId())
        );

        return "redirect:/quests";
    }

    @PutMapping("/{questId}")
    public String update(@Valid @ModelAttribute("quest") QuestRequestDto dto, BindingResult bindingResult, @PathVariable("questId") Long questId, @AuthenticationPrincipal UserPrincipal principal, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("rewards", rewardService.getRewardListByIds(dto.getRewards(), principal.getUserId()));
            model.addAttribute("difficultyList", QuestDifficulty.getEnumList());
            model.addAttribute("rewardList", rewardService.getRewardList(principal.getUserId()));
            model.addAttribute("hasError", true);
            return "quest/view";
        }

        questService.updateQuest(dto, questId, principal.getUserId());

        return "redirect:/quests/{questId}";
    }

    @DeleteMapping("/{questId}")
    public String delete(@PathVariable("questId") Long questId, @AuthenticationPrincipal UserPrincipal principal) {
        questService.deleteQuest(questId, principal.getUserId());
        return "redirect:/quests";
    }

    @PostMapping("/{questId}")
    public String complete(@PathVariable("questId") Long questId, @AuthenticationPrincipal UserPrincipal principal) throws IOException {
        questService.completeQuest(questId, principal);
        return "redirect:/quests";
    }

    @DeleteMapping("/{questId}/discard")
    public String discard(@PathVariable("questId") Long questId, @AuthenticationPrincipal UserPrincipal principal) {
        questService.discardQuest(questId, principal.getUserId());
        return "redirect:/quests";
    }
}
