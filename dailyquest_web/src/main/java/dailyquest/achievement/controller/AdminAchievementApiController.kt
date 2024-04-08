package dailyquest.achievement.controller

import dailyquest.achievement.dto.WebAchievementSaveRequest
import dailyquest.achievement.dto.WebAchievementUpdateRequest
import dailyquest.achievement.service.AchievementCommandService
import dailyquest.common.BatchApiUtil
import jakarta.validation.Valid
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@Validated
@RequestMapping("/admin/api/v1/achievements")
@RestController
class AdminAchievementApiController(
    private val achievementCommandService: AchievementCommandService,
    private val batchApiUtil: BatchApiUtil
) {

    @PostMapping("")
    fun saveAchievement(
        @Valid @RequestBody saveRequest: WebAchievementSaveRequest
    ) {
        val savedAchievementId = achievementCommandService.saveAchievement(saveRequest)
        batchApiUtil.checkAndAchieve(savedAchievementId)
    }

    @PatchMapping("/{achievementId}")
    fun updateAchievement(
        @Valid @RequestBody updateRequest: WebAchievementUpdateRequest,
        @PathVariable("achievementId") achievementId: Long
    ) {
        achievementCommandService.updateAchievement(achievementId, updateRequest)
    }

    @PatchMapping("/{achievementId}/inactivate")
    fun inactivateAchievement(
        @PathVariable achievementId: Long
    ) {
        achievementCommandService.inactivateAchievement(achievementId)
    }

    @PatchMapping("/{achievementId}/activate")
    fun activateAchievement(
        @PathVariable achievementId: Long
    ) {
        achievementCommandService.activateAchievement(achievementId)
    }
}