package todayquest.reward.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import todayquest.reward.dto.RewardRequestDto;
import todayquest.reward.entity.RewardGrade;
import todayquest.reward.service.RewardService;
import todayquest.user.dto.UserPrincipal;

import javax.validation.Valid;

@RequiredArgsConstructor
@RequestMapping("/rewards")
@Controller
public class RewardController {
    private final RewardService rewardService;

    @GetMapping("")
    public String list(Model model, @AuthenticationPrincipal UserPrincipal principal) {
        model.addAttribute("rewards", rewardService.getRewardList(principal.getUserId()));
        return "reward/list";
    }

    @GetMapping("/{rewardId}")
    public String view(@PathVariable("rewardId") Long rewardId, Model model, @AuthenticationPrincipal UserPrincipal principal) {
        model.addAttribute("reward", rewardService.getReward(rewardId, principal.getUserId()));
        model.addAttribute("gradeList", RewardGrade.getEnumList());
        return "reward/view";
    }

    @GetMapping("/save")
    public String saveForm(Model model) {
        model.addAttribute("reward", new RewardRequestDto());
        model.addAttribute("gradeList", RewardGrade.getEnumList());
        return "reward/save";
    }


    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("reward") RewardRequestDto dto, BindingResult bindingResult, @AuthenticationPrincipal UserPrincipal principal, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("gradeList", RewardGrade.getEnumList());
            return "reward/save";
        }

        rewardService.saveReward(dto, principal.getUserId());
        return "redirect:/rewards";
    }

    @PutMapping("/{rewardId}")
    public String update(@Valid @ModelAttribute("reward") RewardRequestDto dto, BindingResult bindingResult, @PathVariable("rewardId") Long rewardId, @AuthenticationPrincipal UserPrincipal principal, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("gradeList", RewardGrade.getEnumList());
            model.addAttribute("hasError", true);
            return "reward/view";
        }
        rewardService.updateReward(dto, rewardId, principal.getUserId());
        return "redirect:/rewards/{rewardId}";
    }

    @DeleteMapping("/{rewardId}")
    public String delete(@PathVariable("rewardId") Long rewardId, @AuthenticationPrincipal UserPrincipal principal) {
        rewardService.deleteReward(rewardId, principal.getUserId());
        return "redirect:/rewards";
    }

}
