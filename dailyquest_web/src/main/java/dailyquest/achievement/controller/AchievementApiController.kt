package dailyquest.achievement.controller

import dailyquest.achievement.dto.AchievementResponse
import dailyquest.achievement.service.AchievementQueryService
import dailyquest.common.ResponseData
import dailyquest.common.RestPage
import dailyquest.user.dto.UserPrincipal
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

@RequestMapping("/api/v1/achievements")
@Controller
class AchievementApiController(
    private val achievementQueryService: AchievementQueryService
) {
    @GetMapping("/achieved")
    fun getAchievedAchievements(
        @RequestParam(required = false, defaultValue = "0") page: Int = 0,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<ResponseData<RestPage<AchievementResponse>>> {
        val achievedAchievements = achievementQueryService.getAchievedAchievements(principal.id, page)
        val wrappedData = ResponseData.of(RestPage(achievedAchievements))
        return ResponseEntity.ok(wrappedData)
    }

    @GetMapping("/not-achieved")
    fun getNotAchievedAchievements(
        @RequestParam(required = false, defaultValue = "0") page: Int,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<ResponseData<RestPage<AchievementResponse>>> {
        val notAchievedAchievements = achievementQueryService.getNotAchievedAchievements(principal.id, page)
        val wrappedData = ResponseData.of(RestPage(notAchievedAchievements))
        return ResponseEntity.ok(wrappedData)
    }
}