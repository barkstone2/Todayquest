package todayquest.user.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import todayquest.common.MessageUtil;
import todayquest.exception.ErrorResponse;
import todayquest.item.service.ItemLogService;
import todayquest.quest.dto.QuestLogSearchCondition;
import todayquest.quest.service.QuestLogService;
import todayquest.user.dto.UserPrincipal;
import todayquest.user.dto.UserRequestDto;
import todayquest.user.service.UserService;

import jakarta.validation.Valid;
import java.io.IOException;
import java.util.Map;

@RequiredArgsConstructor
@RequestMapping("/user")
@Controller
public class UserController {

    private final ResourceLoader resourceLoader;
    private final QuestLogService questLogService;
    private final ItemLogService itemLogService;
    private final UserService userService;

    @GetMapping("/status")
    public String myPage(@ModelAttribute("searchCondition") QuestLogSearchCondition condition, @AuthenticationPrincipal UserPrincipal principal, Model model) throws IOException {

        // 경험치 테이블을 읽어온다.
        Resource resource = resourceLoader.getResource("classpath:data/exp_table.json");
        ObjectMapper om = new ObjectMapper();
        Map<Integer, Long> expTable = om.readValue(resource.getInputStream(), new TypeReference<>() {});

        model.addAttribute("targetExp", expTable.get(principal.getLevel()));
        model.addAttribute("questStatistic", questLogService.getQuestStatistic(principal.getUserId(), condition));
        model.addAttribute("itemLog", itemLogService.getItemLog(principal.getUserId()));
        return "user/status";
    }

    @ResponseBody
    @GetMapping("")
    public String nicknameDuplicateCheck(@RequestParam String nickname) {
        return userService.isDuplicateNickname(nickname) ? "중복된 닉네임입니다." : "닉네임을 사용할 수 있습니다.";
    }

    @ResponseBody
    @PutMapping("")
    public ResponseEntity<String> changeUserSettings(@Valid @RequestBody UserRequestDto dto, @AuthenticationPrincipal UserPrincipal principal) {
        userService.changeUserSettings(principal, dto);

        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(MessageUtil.getMessage("user.settings.changed"));
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler({IllegalStateException.class})
    public ErrorResponse illegalExHandle(IllegalStateException e) {
        return new ErrorResponse(e.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidationExceptions(MethodArgumentNotValidException ex){
        return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON).body(ex.getBindingResult().getAllErrors().get(0).getDefaultMessage());
    }

}
