package dailyquest.achievement.controller

import dailyquest.achievement.dto.AdminAchievementResponse
import dailyquest.achievement.dto.WebAchievementSaveRequest
import dailyquest.achievement.dto.WebAchievementUpdateRequest
import dailyquest.achievement.entity.AchievementType
import dailyquest.achievement.service.AchievementService
import dailyquest.common.ResponseData
import dailyquest.sqs.service.SqsService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@Validated
@RequestMapping("/admin/api/v1/achievements")
@RestController
class AdminAchievementApiController(
    private val achievementService: AchievementService,
    private val sqsService: SqsService
) {

    @PostMapping("")
    fun saveAchievement(
        @Valid @RequestBody saveRequest: WebAchievementSaveRequest
    ) {
        val savedAchievementId = achievementService.saveAchievement(saveRequest)
        sqsService.publishRegisterMessage(savedAchievementId)
    }

    @PatchMapping("/{achievementId}")
    fun updateAchievement(
        @Valid @RequestBody updateRequest: WebAchievementUpdateRequest,
        @PathVariable("achievementId") achievementId: Long
    ) {
        achievementService.updateAchievement(achievementId, updateRequest)
    }

    @PatchMapping("/{achievementId}/inactivate")
    fun inactivateAchievement(
        @PathVariable achievementId: Long
    ) {
        achievementService.inactivateAchievement(achievementId)
    }

    @PatchMapping("/{achievementId}/activate")
    fun activateAchievement(
        @PathVariable achievementId: Long
    ) {
        achievementService.activateAchievement(achievementId)
    }

    @GetMapping("")
    fun getAllAchievementsAndType(): ResponseEntity<ResponseData<AdminAchievementResponse>> {
        val allAchievementsGroupByType = achievementService.getAllAchievementsGroupByType()
        val achievementTypes = AchievementType.values().toList()
        val adminAchievementResponse = AdminAchievementResponse(achievementTypes, allAchievementsGroupByType)
        return ResponseEntity.ok(ResponseData.of(adminAchievementResponse))
    }
}