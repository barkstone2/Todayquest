package todayquest.reward.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import todayquest.annotation.WithCustomMockUser;
import todayquest.config.SecurityConfig;
import todayquest.reward.dto.RewardResponseDto;
import todayquest.reward.entity.Reward;
import todayquest.reward.entity.RewardGrade;
import todayquest.reward.service.RewardService;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("보상 아이템 컨트롤러 유닛 테스트")
@WithCustomMockUser
@WebMvcTest(controllers = RewardController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = SecurityConfig.class
        )
)
class RewardControllerUnitTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    RewardService rewardService;

    static final String URI_PREFIX = "/rewards";

    @DisplayName("보상 아이템 목록 요청")
    @Test
    void testGetList() throws Exception {
        //given
        String uri = "";
        List<RewardResponseDto> rewardList = new ArrayList<>();

        //when
        when(rewardService.getRewardList(any())).thenReturn(rewardList);

        //then
        mvc.perform(get(URI_PREFIX + uri))
                .andExpect(status().isOk())
                .andExpect(model().attribute("rewards", rewardList))
                .andExpect(view().name("/reward/list"));

    }

    @DisplayName("보상 아이템 조회")
    @Test
    void testGetView() throws Exception {
        //given
        Reward reward = Reward.builder().id(1L).name("save name").build();
        String uri = "/" + reward.getId();

        //when
        when(rewardService.getReward(any(), any())).thenReturn(RewardResponseDto.createDto(reward));

        //then
        RewardResponseDto dto = (RewardResponseDto) mvc.perform(get(URI_PREFIX + uri))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("reward"))
                .andExpect(view().name("/reward/view"))
                .andReturn().getModelAndView().getModel().get("reward");

        assertThat(dto.getName()).isEqualTo(reward.getName());
    }

    @DisplayName("보상 아이템 등록 화면")
    @Test
    public void testSaveForm() throws Exception {
        //given
        String uri = "/save";

        //when
        //then
        mvc.perform(get(URI_PREFIX + uri))
                .andExpect(model().attributeExists("gradeList"))
                .andExpect(model().attributeExists("reward"))
                .andExpect(view().name("/reward/save"));

    }

    @DisplayName("보상 아이템 저장_성공")
    @Test
    public void testSave() throws Exception {
        //given
        String uri = "/save";
        MultiValueMap<String, String> param = new LinkedMultiValueMap<>();
        param.add("name", "save name");
        param.add("description", "save description");
        param.add("grade", RewardGrade.E.name());

        //when
        //then
        mvc.perform(post(URI_PREFIX + uri)
                        .with(csrf())
                        .params(param))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/rewards*"));
    }


    @DisplayName("보상 아이템 저장_Validation Fail")
    @Test
    public void testSaveValidationFail() throws Exception {
        //given
        String uri = "/save";
        MultiValueMap<String, String> param = new LinkedMultiValueMap<>();
        param.add("name", "");
        param.add("description", "save description");
        param.add("grade", RewardGrade.E.name());

        //when
        //then
        mvc.perform(post(URI_PREFIX + uri)
                        .with(csrf())
                        .params(param))
                .andExpect(view().name("/reward/save"));
    }



    @DisplayName("보상 아이템 업데이트")
    @Test
    public void testUpdate() throws Exception {
        //given
        Reward reward = Reward.builder().id(1L).name("save name").build();
        String uri = "/" + reward.getId();
        MultiValueMap<String, String> param = new LinkedMultiValueMap<>();
        param.add("name", "update name");
        param.add("description", "save description");
        param.add("grade", RewardGrade.E.name());

        //when
        //then
        mvc.perform(put(URI_PREFIX + uri)
                        .with(csrf())
                        .params(param))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/rewards/" + reward.getId()));
    }

    @DisplayName("보상 아이템 업데이트_Validation Fail")
    @Test
    public void testUpdateValidationFail() throws Exception {
        //given
        Reward reward = Reward.builder().id(1L).name("save name").build();
        String uri = "/" + reward.getId();
        MultiValueMap<String, String> param = new LinkedMultiValueMap<>();
        param.add("name", "update name");

        //when
        //then
        mvc.perform(put(URI_PREFIX + uri)
                        .with(csrf())
                        .params(param))
                .andExpect(view().name("/reward/view"))
                .andExpect(model().attributeExists("hasError"));
    }

    @DisplayName("보상 아이템 삭제")
    @Test
    public void testDelete() throws Exception {
        //given
        Reward reward = Reward.builder().id(1L).name("save name").build();
        String uri = "/" + reward.getId();

        //when
        //then
        mvc.perform(delete(URI_PREFIX + uri)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/rewards"));
    }



}