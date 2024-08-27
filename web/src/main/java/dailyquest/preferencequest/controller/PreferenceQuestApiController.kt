package dailyquest.preferencequest.controller

import dailyquest.common.ResponseData
import dailyquest.common.UserLevelLock
import dailyquest.preferencequest.dto.PreferenceQuestResponse
import dailyquest.preferencequest.dto.WebPreferenceQuestRequest
import dailyquest.preferencequest.service.PreferenceQuestService
import dailyquest.quest.dto.QuestResponse
import dailyquest.search.service.QuestIndexService
import dailyquest.user.dto.UserPrincipal
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@Validated
@RequestMapping("/api/v1/preference/quests")
@RestController
class PreferenceQuestApiController(
    private val preferenceQuestService: PreferenceQuestService,
    private val userLevelLock: UserLevelLock,
    private val questIndexService: QuestIndexService
) {

    @GetMapping("")
    fun getPreferenceQuestList(
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<ResponseData<List<PreferenceQuestResponse>>> {
        return ResponseEntity.ok(ResponseData.of(preferenceQuestService.getActivePreferenceQuests(principal.id)))
    }

    @GetMapping("/{preferenceQuestId}")
    fun getPreferenceQuest(
        @Min(1) @PathVariable("preferenceQuestId") preferenceQuestId: Long,
        @AuthenticationPrincipal principal: UserPrincipal

    ): ResponseEntity<ResponseData<PreferenceQuestResponse>> {
        return ResponseEntity.ok(ResponseData.of(preferenceQuestService.getPreferenceQuest(preferenceQuestId, principal.id)))
    }

    @PostMapping("")
    fun savePreferenceQuest(
        @Valid @RequestBody preferenceQuestRequest: WebPreferenceQuestRequest,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<ResponseData<PreferenceQuestResponse>> {
        return ResponseEntity.ok(ResponseData.of(preferenceQuestService.savePreferenceQuest(preferenceQuestRequest, principal.id)))
    }

    @PatchMapping("/{preferenceQuestId}")
    fun updatePreferenceQuest(
        @Min(1) @PathVariable("preferenceQuestId") preferenceQuestId: Long,
        @Valid @RequestBody preferenceQuestRequest: WebPreferenceQuestRequest,
        @AuthenticationPrincipal principal: UserPrincipal

    ): ResponseEntity<ResponseData<PreferenceQuestResponse>> {
        return ResponseEntity.ok(ResponseData.of(preferenceQuestService.updatePreferenceQuest(preferenceQuestRequest, preferenceQuestId, principal.id)))
    }

    @PatchMapping("/{preferenceQuestId}/delete")
    fun deletePreferenceQuest(
        @Min(1) @PathVariable("preferenceQuestId") preferenceQuestId: Long,
        @AuthenticationPrincipal principal: UserPrincipal
    ) {
        preferenceQuestService.deletePreferenceQuest(preferenceQuestId, principal.id)
    }

    @PostMapping("/{preferenceQuestId}/register")
    fun registerQuestByPreferenceQuest(
        @Min(1) @PathVariable("preferenceQuestId") preferenceQuestId: Long,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<ResponseData<QuestResponse>> {
        val questResponse = userLevelLock.executeWithLock("QUEST_SEQ" + principal.id, 3) {
            preferenceQuestService.registerQuestByPreferenceQuest(preferenceQuestId, principal.id)
        }
        questIndexService.saveDocument(questResponse, principal.id)
        return ResponseEntity.ok(ResponseData.of(questResponse))
    }
}