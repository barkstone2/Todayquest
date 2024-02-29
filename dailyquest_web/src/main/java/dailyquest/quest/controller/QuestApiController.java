package dailyquest.quest.controller;

import dailyquest.achievement.dto.AchievementAchieveRequest;
import dailyquest.achievement.entity.AchievementType;
import dailyquest.achievement.service.AchievementCommandService;
import dailyquest.common.ResponseData;
import dailyquest.common.RestPage;
import dailyquest.common.UserLevelLock;
import dailyquest.quest.dto.*;
import dailyquest.quest.entity.QuestState;
import dailyquest.quest.service.QuestService;
import dailyquest.search.service.QuestIndexService;
import dailyquest.user.dto.UserPrincipal;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

import static dailyquest.achievement.entity.AchievementType.*;

@Validated
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/quests")
@RestController
public class QuestApiController {
    private final QuestService questService;
    private final UserLevelLock userLevelLock;
    private final QuestIndexService questIndexService;
    private final AchievementCommandService achievementCommandService;

    @Value("${quest.page.size}")
    private int pageSize;

    @GetMapping("")
    public ResponseEntity<ResponseData<List<QuestResponse>>> getQuestList(
            QuestState state,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        List<QuestResponse> questList = questService.getCurrentQuests(principal.getId(), state);
        return ResponseEntity.ok(new ResponseData<>(questList));
    }

    @GetMapping("/search")
    public ResponseEntity<ResponseData<RestPage<QuestResponse>>> searchQuest(
            @Valid QuestSearchCondition searchCondition,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        RestPage<QuestResponse> questList;
        PageRequest pageable = PageRequest.of(searchCondition.page(), pageSize);
        if(searchCondition.isKeywordSearch()) {
            List<Long> searchedIds = questIndexService.searchDocuments(searchCondition, principal.getId(), pageable);
            questList = questService.searchQuest(searchedIds, pageable);
        } else {
            questList = questService.searchQuest(principal.getId(), searchCondition, pageable);
        }
        return ResponseEntity.ok(new ResponseData<>(questList));
    }

    @GetMapping("/{questId}")
    public ResponseEntity<ResponseData<QuestResponse>> getQuest(
            @Min(1) @PathVariable("questId") Long questId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        QuestResponse quest = questService.getQuestInfo(questId, principal.getId());
        return ResponseEntity.ok(new ResponseData<>(quest));
    }

    @PostMapping("")
    public ResponseEntity<ResponseData<QuestResponse>> saveQuest(
            @Valid @RequestBody QuestRequest dto,
            @AuthenticationPrincipal UserPrincipal principal
    ) {

        QuestResponse savedQuest = userLevelLock.executeWithLock(
                "QUEST_SEQ" + principal.getId(),
                3,
                () -> questService.saveQuest(dto, principal.getId())
        );
        questIndexService.saveDocument(savedQuest, principal.getId());
        achievementCommandService.checkAndAchieveAchievement(AchievementAchieveRequest.of(QUEST_REGISTRATION, principal.getId()));
        achievementCommandService.checkAndAchieveAchievement(AchievementAchieveRequest.of(QUEST_CONTINUOUS_REGISTRATION_DAYS, principal.getId()));
        return ResponseEntity.ok(new ResponseData<>(savedQuest));
    }

    @PatchMapping("/{questId}")
    public ResponseEntity<ResponseData<QuestResponse>> update(
            @Valid @RequestBody QuestRequest dto,
            @Min(1) @PathVariable("questId") Long questId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {

        QuestResponse updatedQuest = questService.updateQuest(dto, questId, principal.getId());
        questIndexService.saveDocument(updatedQuest, principal.getId());
        return new ResponseEntity<>(new ResponseData<>(updatedQuest), HttpStatus.OK);
    }

    @PatchMapping("/{questId}/delete")
    public ResponseEntity<ResponseData<Void>> delete(
            @Min(1) @PathVariable("questId") Long questId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        QuestResponse deletedQuest = questService.deleteQuest(questId, principal.getId());
        questIndexService.deleteDocument(deletedQuest);
        return ResponseEntity.ok(new ResponseData<>());
    }

    @PatchMapping("/{questId}/complete")
    public ResponseEntity<ResponseData<Void>> complete(
            @Min(1) @PathVariable("questId") Long questId,
            @AuthenticationPrincipal UserPrincipal principal
    ) throws IOException {
        QuestResponse completedQuest = questService.completeQuest(questId, principal.getId());
        questIndexService.updateQuestStateOfDocument(completedQuest, principal.getId());
        achievementCommandService.checkAndAchieveAchievement(AchievementAchieveRequest.of(QUEST_COMPLETION, principal.getId()));
        achievementCommandService.checkAndAchieveAchievement(AchievementAchieveRequest.of(USER_LEVEL, principal.getId()));
        return ResponseEntity.ok(new ResponseData<>());
    }

    @PatchMapping("/{questId}/discard")
    public ResponseEntity<ResponseData<Void>> discard(
            @Min(1) @PathVariable("questId") Long questId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        QuestResponse questResponse = questService.discardQuest(questId, principal.getId());
        questIndexService.updateQuestStateOfDocument(questResponse, principal.getId());
        return ResponseEntity.ok(new ResponseData<>());
    }

    @PatchMapping(value = "/{questId}/details/{detailQuestId}")
    public ResponseEntity<ResponseData<DetailResponse>> updateDetailQuestCount(
            @Min(1) @PathVariable("questId") Long questId,
            @Min(1) @PathVariable("detailQuestId") Long detailQuestId,
            @Valid @RequestBody(required = false) DetailInteractRequest requestDto,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        if (requestDto == null) requestDto = new DetailInteractRequest();
        requestDto.setPathVariables(questId, detailQuestId);
        DetailResponse interactDetail = questService.updateDetailQuestCount(principal.getId(), requestDto);
        return ResponseEntity.ok(new ResponseData<>(interactDetail));
    }

}
