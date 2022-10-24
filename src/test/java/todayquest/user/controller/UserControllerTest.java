package todayquest.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.MessageSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;
import todayquest.annotation.WithCustomMockUser;
import todayquest.common.DatabaseCleanup;
import todayquest.common.MessageUtil;
import todayquest.user.dto.UserRequestDto;
import todayquest.user.entity.ProviderType;
import todayquest.user.entity.UserInfo;
import todayquest.user.repository.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("유저 컨트롤러 통합 테스트")
@Slf4j
@WithCustomMockUser(userId = 1L)
@Transactional
@SpringBootTest(webEnvironment = RANDOM_PORT)
public class UserControllerTest {

    @LocalServerPort
    int port;

    static final String SERVER_ADDR = "http://localhost:";
    static final String URI_PREFIX = "/user";

    @Autowired
    WebApplicationContext context;

    @Autowired
    UserRepository userRepository;

    @Autowired
    DatabaseCleanup databaseCleanup;

    @Autowired
    MessageSource messageSource;

    MessageUtil messageUtil;

    MockMvc mvc;
    UserInfo testUser;

    @BeforeEach
    public void setUp() {
        databaseCleanup.execute();
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .addFilter(new CharacterEncodingFilter("UTF-8", true))
                .build();
        messageUtil = new MessageUtil(messageSource);
        testUser = UserInfo.builder().id(1L).build();
    }

    @DisplayName("상태창 조회 통합 테스트")
    @Test
    public void testMyPage() throws Exception {
        //given
        String url = SERVER_ADDR + port + URI_PREFIX + "/status";

        //when
        //then
        mvc.perform(get(url))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("targetExp"))
                .andExpect(model().attributeExists("questLog"))
                .andExpect(model().attributeExists("itemLog"))
                .andExpect(view().name("user/status"));

    }

    @DisplayName("닉네임 변경 API 테스트_성공")
    @Test
    public void testChangeNicknameApi() throws Exception {
        //given
        String url = SERVER_ADDR + port + URI_PREFIX;

        // before nickname => nickname
        String nickname = "newNickname";
        UserRequestDto dto = new UserRequestDto();
        dto.setNickname(nickname);

        ObjectMapper om = new ObjectMapper();

        //when
        mvc.perform(put(url)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(content().string(MessageUtil.getMessage("nickname.changed")));

        //then
        UserInfo findUser = userRepository.getById(1L);
        assertThat(findUser.getNickname()).isEqualTo(nickname);
    }

    @DisplayName("닉네임 변경 API 테스트_닉네임 중복")
    @Test
    public void testChangeNicknameApiDuplicate() throws Exception {
        //given
        String url = SERVER_ADDR + port + URI_PREFIX;

        userRepository.save(UserInfo.builder().nickname("newNickname").oauth2Id("oid2").providerType(ProviderType.GOOGLE).build());

        // before nickname => nickname
        String nickname = "newNickname";
        UserRequestDto dto = new UserRequestDto();
        dto.setNickname(nickname);

        ObjectMapper om = new ObjectMapper();

        //when
        mvc.perform(put(url)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(dto)))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(content().string(MessageUtil.getMessage("nickname.duplicate")));

        //then
        UserInfo findUser = userRepository.getById(1L);
        assertThat(findUser.getNickname()).isNotEqualTo(nickname);
    }

    @DisplayName("닉네임 변경 API 테스트_닉네임 패턴 에러")
    @Test
    public void testChangeNicknameApiPattern() throws Exception {
        //given
        String url = SERVER_ADDR + port + URI_PREFIX;
        String nickname = "ㅏㅏㅏㅏ";
        UserRequestDto dto = new UserRequestDto();
        dto.setNickname(nickname);

        ObjectMapper om = new ObjectMapper();

        //when
        mvc.perform(put(url)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(content().string(MessageUtil.getMessage("nickname.pattern")));

        //then
        UserInfo findUser = userRepository.getById(1L);
        assertThat(findUser.getNickname()).isNotEqualTo(nickname);
    }


}
