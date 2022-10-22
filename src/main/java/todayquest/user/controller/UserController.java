package todayquest.user.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import todayquest.common.MessageUtil;
import todayquest.item.service.ItemLogService;
import todayquest.quest.service.QuestLogService;
import todayquest.user.dto.UserPrincipal;
import todayquest.user.dto.UserRequestDto;
import todayquest.user.service.UserService;

import javax.validation.Valid;
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
    public String myPage(@AuthenticationPrincipal UserPrincipal principal, Model model) throws IOException {

        // 경험치 테이블을 읽어온다.
        Resource resource = resourceLoader.getResource("classpath:data/exp_table.json");
        ObjectMapper om = new ObjectMapper();
        Map<Integer, Long> expTable = om.readValue(resource.getInputStream(), new TypeReference<>() {});

        model.addAttribute("targetExp", expTable.get(principal.getLevel()));
        model.addAttribute("questLog", questLogService.getQuestLog(principal.getUserId()));
        model.addAttribute("itemLog", itemLogService.getItemLog(principal.getUserId()));
        return "user/status";
    }

    @ResponseBody
    @PutMapping("")
    public ResponseEntity<String> changeNickname(@Valid @RequestBody UserRequestDto user, @AuthenticationPrincipal UserPrincipal principal) {

        String nickname = user.getNickname().trim();
        boolean isDuplicated = userService.isDuplicateNickname(nickname);
        ResponseEntity<String> responseEntity;

        if (isDuplicated) {
            responseEntity = new ResponseEntity<>(MessageUtil.getMessage("nickname.duplicate"), HttpStatus.CONFLICT);
        } else {
            userService.changeNickname(principal, nickname);
            responseEntity = new ResponseEntity<>(MessageUtil.getMessage("nickname.changed"), HttpStatus.OK);
        }

        return responseEntity;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidationExceptions(MethodArgumentNotValidException ex){
        return ResponseEntity.badRequest().body(ex.getBindingResult().getAllErrors().get(0).getDefaultMessage());
    }

}
