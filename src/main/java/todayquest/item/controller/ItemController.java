package todayquest.item.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import todayquest.common.MessageUtil;
import todayquest.item.dto.ItemRequestDto;
import todayquest.item.dto.ItemResponseDto;
import todayquest.item.service.ItemService;
import todayquest.user.dto.UserPrincipal;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@RequestMapping("/inventory")
@Controller
public class ItemController {

    private final ItemService itemService;

    @GetMapping("")
    public String inventory(@AuthenticationPrincipal UserPrincipal principal, Model model) {
        model.addAttribute("items", itemService.getInventoryItems(principal.getUserId()));
        return "item/inventory";
    }

    @GetMapping("/{itemId}")
    public String itemInfo(@PathVariable Long itemId, @AuthenticationPrincipal UserPrincipal principal, Model model) {
        model.addAttribute("item", itemService.getItemInfo(itemId, principal.getUserId()));
        return "item/view";
    }


    @ResponseBody
    @PutMapping("/{itemId}")
    public ResponseEntity<Object> itemUse(@RequestBody ItemRequestDto dto, @PathVariable Long itemId, @AuthenticationPrincipal UserPrincipal principal) {
        ItemResponseDto usedItem = itemService.useItem(itemId, principal.getUserId(), dto.getCount());
        Map<String, Object> result = new HashMap<>();
        result.put("message", MessageUtil.getMessage("item.use.success", usedItem.getName(), dto.getCount(), usedItem.getCount()));
        result.put("remain_count", usedItem.getCount());

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @ResponseBody
    @DeleteMapping("/{itemId}")
    public ResponseEntity<Object> itemAbandon(@RequestBody ItemRequestDto dto, @PathVariable Long itemId, @AuthenticationPrincipal UserPrincipal principal) {
        ItemResponseDto abandonItem = itemService.abandonItem(itemId, principal.getUserId(), dto.getCount());
        Map<String, Object> result = new HashMap<>();
        result.put("message", MessageUtil.getMessage("item.abandon.success", abandonItem.getName(), dto.getCount(), abandonItem.getCount()));
        result.put("remain_count", abandonItem.getCount());

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @ExceptionHandler({IllegalArgumentException.class})
    public ResponseEntity<Object> illegalExHandle(IllegalArgumentException e) {
        Map<String, Object> result = new HashMap<>();
        result.put("message", e.getMessage());

        return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
    }

}
