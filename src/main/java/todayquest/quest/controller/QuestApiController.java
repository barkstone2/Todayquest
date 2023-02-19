package todayquest.quest.controller;

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
import todayquest.common.ResponseData;
import todayquest.common.RestPage;
import todayquest.common.UserLevelLock;
import todayquest.quest.dto.*;
import todayquest.quest.service.QuestService;
import todayquest.user.dto.UserPrincipal;

import java.io.IOException;

@Validated
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/quests")
@RestController
public class QuestApiController {
    private final QuestService questService;
    private final UserLevelLock userLevelLock;

    @Value("${quest.page.size}")
    private int pageSize;

    @GetMapping("")
    public ResponseEntity<ResponseData<RestPage<QuestResponse>>> getQuestList(
            @Valid QuestSearchCondition condition,
            @AuthenticationPrincipal UserPrincipal principal
    ) {

        RestPage<QuestResponse> questList = questService.getQuestList(
                principal.getUserId(),
                condition.getState(),
                PageRequest.of(condition.getPage(), pageSize)
        );

        return ResponseEntity.ok(new ResponseData<>(questList));
    }

    @GetMapping("/{questId}")
    public ResponseEntity<ResponseData<QuestResponse>> getQuest(
            @Min(1) @PathVariable("questId") Long questId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        QuestResponse quest = questService.getQuestInfo(questId, principal.getUserId());
        return ResponseEntity.ok(new ResponseData<>(quest));
    }

    @PostMapping("")
    public ResponseEntity<ResponseData<QuestResponse>> saveQuest(
            @Valid @RequestBody QuestRequest dto,
            @AuthenticationPrincipal UserPrincipal principal
    ) {

        QuestResponse savedQuest = userLevelLock.executeWithLock(
                "QUEST_SEQ" + principal.getUserId(),
                3,
                () -> questService.saveQuest(dto, principal.getUserId())
        );

        return ResponseEntity.ok(new ResponseData<>(savedQuest));
    }

    @PatchMapping("/{questId}")
    public ResponseEntity<ResponseData<QuestResponse>> update(
            @Valid @RequestBody QuestRequest dto,
            @Min(1) @PathVariable("questId") Long questId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {

        QuestResponse updatedQuest = questService.updateQuest(dto, questId, principal.getUserId());

        return new ResponseEntity<>(new ResponseData<>(updatedQuest), HttpStatus.OK);
    }

    @PatchMapping("/{questId}/delete")
    public ResponseEntity<ResponseData<Void>> delete(
            @Min(1) @PathVariable("questId") Long questId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        questService.deleteQuest(questId, principal.getUserId());
        return ResponseEntity.ok(new ResponseData<>());
    }

    @PatchMapping("/{questId}/complete")
    public ResponseEntity<ResponseData<Void>> complete(
            @Min(1) @PathVariable("questId") Long questId,
            @AuthenticationPrincipal UserPrincipal principal
    ) throws IOException {
        questService.completeQuest(questId, principal.getUserId());
        return ResponseEntity.ok(new ResponseData<>());
    }

    @PatchMapping("/{questId}/discard")
    public ResponseEntity<ResponseData<Void>> discard(
            @Min(1) @PathVariable("questId") Long questId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        questService.discardQuest(questId, principal.getUserId());
        return ResponseEntity.ok(new ResponseData<>());
    }

    @PatchMapping(value = "/{questId}/details/{detailQuestId}", consumes = {"application/json"})
    public ResponseEntity<ResponseData<DetailResponse>> interactWithDetailQuest(
            @Min(1) @PathVariable("questId") Long questId,
            @Min(1) @PathVariable("detailQuestId") Long detailQuestId,
            @Valid @RequestBody(required = false) DetailInteractRequest dto,
            @AuthenticationPrincipal UserPrincipal principal
    ) {

        DetailResponse interactDetail = questService.interactWithDetailQuest(principal.getUserId(), questId, detailQuestId, dto);

        return ResponseEntity.ok(new ResponseData<>(interactDetail));
    }

}
