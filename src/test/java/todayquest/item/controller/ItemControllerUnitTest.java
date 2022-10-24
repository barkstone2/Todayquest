package todayquest.item.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import todayquest.annotation.WithCustomMockUser;
import todayquest.common.MessageUtil;
import todayquest.config.SecurityConfig;
import todayquest.item.dto.ItemRequestDto;
import todayquest.item.dto.ItemResponseDto;
import todayquest.item.service.ItemService;
import todayquest.reward.entity.RewardGrade;

import java.util.ArrayList;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("아이템 컨트롤러 유닛 테스트")
@WithCustomMockUser
@WebMvcTest(controllers = ItemController.class,
excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = SecurityConfig.class
    )
)
public class ItemControllerUnitTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    ItemService itemService;

    static final String URI_PREFIX = "/inventory";

    @DisplayName("인벤토리 조회")
    @Test
    public void testInventory() throws Exception {
        //given
        String uri = "";
        when(itemService.getInventoryItems(any())).thenReturn(new ArrayList<>());

        //when
        mvc.perform(get(URI_PREFIX + uri))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("items"))
                .andExpect(view().name("item/inventory"));

        //then
        verify(itemService).getInventoryItems(any());
    }

    @DisplayName("아이템 정보 조회")
    @Test
    public void testItemInfo() throws Exception {
        //given
        Long itemId = 1L;
        String uri = "/" + itemId;
        ItemResponseDto dto = ItemResponseDto.builder()
                .id(1L).count(1).description("des")
                .grade(RewardGrade.E).name("name")
                .build();

        when(itemService.getItemInfo(eq(itemId), any()))
                .thenReturn(dto);

        //when
        mvc.perform(get(URI_PREFIX + uri))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("item"))
                .andExpect(view().name("item/view"));

        //then
        verify(itemService).getItemInfo(eq(itemId), any());
    }

    @DisplayName("아이템 사용 API 테스트")
    @Test
    public void testItemUse() throws Exception {
        //given
        Long itemId = 1L;
        String uri = "/" + itemId;

        int usedCount = 1;
        int remainCount = 0;

        ObjectMapper om = new ObjectMapper();
        ItemRequestDto requestDto = new ItemRequestDto();
        requestDto.setCount(usedCount);
        String requestBody = om.writeValueAsString(requestDto);

        ItemResponseDto responseDto = ItemResponseDto.builder()
                .id(1L).count(remainCount).description("des")
                .grade(RewardGrade.E).name("name")
                .build();

        when(itemService.useItem(eq(itemId), any(), eq(usedCount)))
                .thenReturn(responseDto);
        String responseBody;

        try (MockedStatic<MessageUtil> messageUtil = mockStatic(MessageUtil.class)) {
            when(MessageUtil.getMessage(anyString())).thenReturn("");

            //when
            responseBody = mvc.perform(put(URI_PREFIX + uri)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        }

        //then
        verify(itemService).useItem(eq(itemId), any(), eq(usedCount));
        Map<String, String> result = om.readValue(responseBody, new TypeReference<>() {});
        assertThat(result.get("remain_count")).isEqualTo(String.valueOf(remainCount));

    }

    @DisplayName("아이템 버리기 API 테스트")
    @Test
    public void testItemAbandon() throws Exception {
        //given
        Long itemId = 1L;
        String uri = "/" + itemId;

        int usedCount = 1;
        int remainCount = 0;

        ObjectMapper om = new ObjectMapper();
        ItemRequestDto requestDto = new ItemRequestDto();
        requestDto.setCount(usedCount);
        String requestBody = om.writeValueAsString(requestDto);

        ItemResponseDto responseDto = ItemResponseDto.builder()
                .id(1L).count(remainCount).description("des")
                .grade(RewardGrade.E).name("name")
                .build();

        when(itemService.abandonItem(eq(itemId), any(), eq(usedCount)))
                .thenReturn(responseDto);
        String responseBody;

        try (MockedStatic<MessageUtil> messageUtil = mockStatic(MessageUtil.class)) {
            when(MessageUtil.getMessage(anyString())).thenReturn("");

            //when
            responseBody = mvc.perform(delete(URI_PREFIX + uri)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        }

        //then
        verify(itemService).abandonItem(eq(itemId), any(), eq(usedCount));
        Map<String, String> result = om.readValue(responseBody, new TypeReference<>() {});
        assertThat(result.get("remain_count")).isEqualTo(String.valueOf(remainCount));

    }



}
