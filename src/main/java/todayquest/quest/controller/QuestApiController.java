package todayquest.quest.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import todayquest.common.ResponseData;
import todayquest.common.RestPage;
import todayquest.common.UserLevelLock;
import todayquest.quest.dto.*;
import todayquest.quest.service.DetailQuestService;
import todayquest.quest.service.QuestService;
import todayquest.user.dto.UserPrincipal;

import java.io.IOException;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/quests")
@RestController
public class QuestApiController {
    private final QuestService questService;
    private final DetailQuestService detailQuestService;
    private final UserLevelLock userLevelLock;

    @GetMapping("")
    public ResponseEntity<ResponseData<RestPage<QuestResponse>>> getQuestList(
            @Valid QuestSearchCondition condition,
            @AuthenticationPrincipal UserPrincipal principal
    ) {

        RestPage<QuestResponse> questList = questService.getQuestList(
                principal.getUserId(),
                condition.getState(),
                PageRequest.of(condition.getPage(), 9)
        );

        return ResponseEntity.ok(new ResponseData<>(questList));
    }

    @GetMapping("/{questId}")
    public ResponseEntity<QuestResponse> getQuest(
            @PathVariable("questId") Long questId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        QuestResponse quest = questService.getQuestInfo(questId, principal.getUserId());
        return ResponseEntity.ok(quest);
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

        return new ResponseEntity<>(new ResponseData<>(savedQuest), HttpStatus.OK);
    }

    @PatchMapping("/{questId}")
    public ResponseEntity<ResponseData<QuestResponse>> update(
            @Valid @RequestBody QuestRequest dto,
            @PathVariable("questId") Long questId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {

        QuestResponse updatedQuest = questService.updateQuest(dto, questId, principal.getUserId());

        return new ResponseEntity<>(new ResponseData<>(updatedQuest), HttpStatus.OK);
    }

    @PatchMapping("/{questId}/delete")
    public ResponseEntity<ResponseData<Void>> delete(
            @PathVariable("questId") Long questId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        questService.deleteQuest(questId, principal.getUserId());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PatchMapping("/{questId}/complete")
    public ResponseEntity<ResponseData<Void>> complete(
            @PathVariable("questId") Long questId,
            @AuthenticationPrincipal UserPrincipal principal
    ) throws IOException {
        questService.completeQuest(questId, principal);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PatchMapping("/{questId}/discard")
    public ResponseEntity<ResponseData<Void>> discard(
            @PathVariable("questId") Long questId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        questService.discardQuest(questId, principal.getUserId());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PatchMapping(value = "/{questId}/details/{detailQuestId}", consumes = {"application/json"})
    public ResponseEntity<ResponseData<DetailResponse>> interactWithDetailQuest(
            @PathVariable("questId") Long questId,
            @PathVariable("detailQuestId") Long detailQuestId,
            @RequestBody(required = false) DetailInteractRequest dto,
            @AuthenticationPrincipal UserPrincipal principal
    ) {

        DetailResponse interactDetail = detailQuestService.interact(principal.getUserId(), questId, detailQuestId, dto);

        return new ResponseEntity<>(new ResponseData<>(interactDetail), HttpStatus.OK);
    }

}
