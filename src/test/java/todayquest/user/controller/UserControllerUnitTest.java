package todayquest.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import todayquest.annotation.WithCustomMockUser;
import todayquest.common.MessageUtil;
import todayquest.config.SecurityConfig;
import todayquest.item.service.ItemLogService;
import todayquest.quest.service.QuestLogService;
import todayquest.user.dto.UserRequestDto;
import todayquest.user.service.UserService;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Slf4j
@DisplayName("유저 컨트롤러 유닛 테스트")
@WithCustomMockUser
@WebMvcTest(controllers = UserController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = SecurityConfig.class
        )
)
public class UserControllerUnitTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    ResourceLoader resourceLoader;

    @MockBean
    QuestLogService questLogService;

    @MockBean
    ItemLogService itemLogService;

    @MockBean
    UserService userService;

    static final String URI_PREFIX = "/user";

    @DisplayName("상태창 조회 테스트")
    @Test
    public void testMyPage() throws Exception {
        //given
        Long userId = 1L;
        String uri = "/status";
        Map<String, Long> questLog = Map.of(
                "COMPLETE", 10L,
                "FAIL",5L,
                "DISCARD", 3L);
        Map<String, Long> itemLog = Map.of(
                "EARN", 5L,
                "USE",2L,
                "ABANDON", 1L);

        when(resourceLoader.getResource(anyString())).thenReturn(new ClassPathResource("data/exp_table.json"));
        when(questLogService.getQuestLog(userId)).thenReturn(questLog);
        when(itemLogService.getItemLog(userId)).thenReturn(itemLog);


        //when
        //then
        mvc.perform(get(URI_PREFIX + uri))
                .andExpect(status().isOk())
                .andExpect(model().attribute("questLog", questLog))
                .andExpect(model().attribute("itemLog", itemLog))
                .andExpect(view().name("user/status"));

        verify(questLogService).getQuestLog(userId);
        verify(itemLogService).getItemLog(userId);
    }

    @DisplayName("닉네임 변경 API 테스트_성공")
    @Test
    public void testChangeNicknameApi() throws Exception {
        //given
        String uri = "";
        String nickname = "newNickname";
        UserRequestDto dto = new UserRequestDto();
        dto.setNickname(nickname);

        ObjectMapper om = new ObjectMapper();

        try (MockedStatic<MessageUtil> messageUtil = mockStatic(MessageUtil.class)) {
            when(userService.isDuplicateNickname(nickname)).thenReturn(false);
            when(MessageUtil.getMessage(anyString())).thenReturn("");

            //when
            mvc.perform(put(URI_PREFIX + uri)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(om.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(content().string(MessageUtil.getMessage("nickname.changed")));
        }

        //then
        verify(userService).isDuplicateNickname(nickname);
        verify(userService).changeNickname(any(), eq(nickname));
    }

    @DisplayName("닉네임 변경 API 테스트_닉네임 중복")
    @Test
    public void testChangeNicknameApiDuplicate() throws Exception {
        //given
        String uri = "";
        String nickname = "newNickname";
        UserRequestDto dto = new UserRequestDto();
        dto.setNickname(nickname);

        ObjectMapper om = new ObjectMapper();

        try (MockedStatic<MessageUtil> messageUtil = mockStatic(MessageUtil.class)) {
            when(userService.isDuplicateNickname(nickname)).thenReturn(true);
            when(MessageUtil.getMessage(anyString())).thenReturn("");

            //when
            mvc.perform(put(URI_PREFIX + uri)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(om.writeValueAsString(dto)))
                    .andExpect(status().isConflict())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(content().string(MessageUtil.getMessage("nickname.changed")));
        }

        //then
        verify(userService).isDuplicateNickname(nickname);
        verifyNoMoreInteractions(userService);

    }



}
