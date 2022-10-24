package todayquest.item.controller;

import com.fasterxml.jackson.core.type.TypeReference;
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
import todayquest.item.dto.ItemRequestDto;
import todayquest.item.dto.ItemResponseDto;
import todayquest.item.entity.Item;
import todayquest.item.repository.ItemRepository;
import todayquest.item.service.ItemService;
import todayquest.quest.entity.Quest;
import todayquest.quest.entity.QuestDifficulty;
import todayquest.quest.entity.QuestState;
import todayquest.quest.entity.QuestType;
import todayquest.reward.entity.Reward;
import todayquest.reward.entity.RewardGrade;
import todayquest.reward.repository.RewardRepository;
import todayquest.user.entity.UserInfo;
import todayquest.user.repository.UserRepository;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Slf4j
@DisplayName("아이템 컨트롤러 통합 테스트")
@WithCustomMockUser(userId = 1L)
@Transactional
@SpringBootTest(webEnvironment = RANDOM_PORT)
public class ItemControllerTest {

    @LocalServerPort
    int port;

    @Autowired ItemService itemService;
    @Autowired UserRepository userRepository;
    @Autowired ItemRepository itemRepository;
    @Autowired RewardRepository rewardRepository;

    @Autowired
    WebApplicationContext context;

    @Autowired
    MessageSource messageSource;

    MessageUtil messageUtil;
    @Autowired
    DatabaseCleanup databaseCleanup;

    MockMvc mvc;

    UserInfo testUser;
    Item testItem;

    static final String SERVER_ADDR = "http://localhost:";
    static final String URI_PREFIX = "/inventory";

    @BeforeEach
    public void setUp() {
        databaseCleanup.execute();
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .addFilter(new CharacterEncodingFilter("UTF-8", true))
                .build();
        messageUtil = new MessageUtil(messageSource);

        testUser = userRepository.getById(1L);
        Reward savedReward = rewardRepository.save(
                Reward.builder()
                        .name("reward")
                        .grade(RewardGrade.E)
                        .user(testUser)
                        .build()
        );

        testItem = itemRepository.save(
                Item.builder()
                        .user(testUser).count(1)
                        .reward(savedReward)
                        .build());
    }

    @DisplayName("인벤토리 조회 통합 테스트")
    @Test
    public void testInventory() throws Exception {
        //given
        String url = SERVER_ADDR + port + URI_PREFIX;

        //when
        //then
        mvc.perform(get(url))
                .andExpect(status().isOk())
                .andExpect(view().name("item/inventory"))
                .andExpect(model().attributeExists("items"));

    }

    @DisplayName("아이템 정보 조회 통합 테스트")
    @Test
    public void testItemInfo() throws Exception {
        //given
        Long itemId = testItem.getId();
        String url = SERVER_ADDR + port + URI_PREFIX + "/" + itemId;

        //when
        //then
        mvc.perform(get(url))
                .andExpect(status().isOk())
                .andExpect(view().name("item/view"))
                .andExpect(model().attributeExists("item"));

    }


    @DisplayName("아이템 사용 통합 테스트")
    @Test
    public void testItemUse() throws Exception {
        //given
        Long itemId = testItem.getId();
        String url = SERVER_ADDR + port + URI_PREFIX + "/" + itemId;

        String itemName = testItem.getReward().getName();
        int usedCount = 1;
        String remainCount = "0";

        ObjectMapper om = new ObjectMapper();
        ItemRequestDto requestDto = new ItemRequestDto();
        requestDto.setCount(usedCount);

        String requestBody = om.writeValueAsString(requestDto);

        //when
        String responseBody = mvc.perform(put(url)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        //then
        Map<String, String> result = om.readValue(responseBody, new TypeReference<>() {});

        assertThat(result.get("remain_count")).isEqualTo(remainCount);
        assertThat(result.get("message"))
                .isEqualTo(
                        MessageUtil.getMessage(
                                "item.use.success",
                                itemName,
                                usedCount,
                                remainCount));

    }

    @DisplayName("아이템 버리기 통합 테스트")
    @Test
    public void testItemAbandon() throws Exception {
        //given
        Long itemId = testItem.getId();
        String url = SERVER_ADDR + port + URI_PREFIX + "/" + itemId;

        String itemName = testItem.getReward().getName();
        int usedCount = 1;
        String remainCount = "0";

        ObjectMapper om = new ObjectMapper();
        ItemRequestDto requestDto = new ItemRequestDto();
        requestDto.setCount(usedCount);

        String requestBody = om.writeValueAsString(requestDto);

        //when
        String responseBody = mvc.perform(delete(url)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        //then
        Map<String, String> result = om.readValue(responseBody, new TypeReference<>() {});

        assertThat(result.get("remain_count")).isEqualTo(remainCount);
        assertThat(result.get("message"))
                .isEqualTo(
                        MessageUtil.getMessage(
                                "item.abandon.success",
                                itemName,
                                usedCount,
                                remainCount));

    }




}
