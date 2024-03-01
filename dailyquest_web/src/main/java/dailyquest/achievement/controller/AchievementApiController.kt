package dailyquest.achievement.controller

import dailyquest.achievement.dto.AchievementResponse
import dailyquest.achievement.entity.AchievementType
import dailyquest.achievement.service.AchievementQueryService
import dailyquest.common.ResponseData
import dailyquest.user.dto.UserPrincipal
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

@RequestMapping("/api/v1/achievements")
@Controller
class AchievementApiController(
    private val achievementQueryService: AchievementQueryService
) {

    @GetMapping("")
    fun getAchievementsWithAchieveInfo(
        achievementType: AchievementType,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<ResponseData<List<AchievementResponse>>> {
        val achievementsWithAchieveInfo =
            achievementQueryService.getAchievementsWithAchieveInfo(achievementType, principal.id)
        val wrappedData = ResponseData.of(achievementsWithAchieveInfo)
        return ResponseEntity.ok(wrappedData)
    }
}