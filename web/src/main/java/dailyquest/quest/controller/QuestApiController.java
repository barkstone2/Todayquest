package dailyquest.quest.controller;

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
import org.hibernate.validator.constraints.Range;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@Validated
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/quests")
@RestController
public class QuestApiController {
    private final QuestService questService;
    private final UserLevelLock userLevelLock;
    private final QuestIndexService questIndexService;

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
            questList = new RestPage<>(questService.searchQuest(searchedIds, pageable));
        } else {
            questList = new RestPage<>(questService.searchQuest(principal.getId(), searchCondition, pageable));
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
            @Valid @RequestBody WebQuestRequest dto,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        if (principal.isNowCoreTime()) {
            dto.toMainQuest();
        }

        QuestResponse savedQuest = userLevelLock.executeWithLock(
                "QUEST_SEQ" + principal.getId(),
                3,
                () -> questService.saveQuest(dto, principal.getId())
        );
        questIndexService.saveDocument(savedQuest, principal.getId());
        return ResponseEntity.ok(new ResponseData<>(savedQuest));
    }

    @PatchMapping("/{questId}")
    public ResponseEntity<ResponseData<QuestResponse>> update(
            @Valid @RequestBody WebQuestRequest dto,
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
        QuestResponse completedQuest = questService.completeQuest(principal.getId(), questId);
        questIndexService.updateQuestStateOfDocument(completedQuest, principal.getId());
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
            @RequestBody(required = false)
            @Range(min = 0, max = 255, message = "{Range.details.count}")
            Integer count,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        DetailInteractRequest interactRequest = new DetailInteractRequest(questId, detailQuestId, count);
        DetailResponse interactDetail = questService.updateDetailQuestCount(principal.getId(), interactRequest);
        return ResponseEntity.ok(new ResponseData<>(interactDetail));
    }

}
