package dailyquest.achivement.controller

import com.ninjasquad.springmockk.MockkBean
import dailyquest.achievement.controller.AchievementApiController
import dailyquest.achievement.service.AchievementQueryService
import dailyquest.annotation.WebMvcUnitTest
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get

@DisplayName("업적 API 컨트롤러 유닛 테스트")
@WebMvcUnitTest([AchievementApiController::class])
class AchievementApiControllerUnitTest {
    companion object {
        const val URI_PREFIX = "/api/v1/achievements"
    }

    @Autowired
    lateinit var mvc: MockMvc

    @MockkBean(relaxed = true)
    lateinit var achievementQueryService: AchievementQueryService

    @DisplayName("달성한 업적 목록 조회 시 page 번호가 제대로 처리된다")
    @Test
    fun `달성한 업적 목록 조회 시 page 번호가 제대로 처리된다`() {
        //given
        val url = "$URI_PREFIX/achieved"
        val page = 1
        every { achievementQueryService.getAchievedAchievements(any(), any()) } returns Page.empty()

        //when
        mvc.perform(
            get(url)
                .queryParam("page", page.toString())
        )

        //then
        verify { achievementQueryService.getAchievedAchievements(any(), eq(page)) }
    }

    @DisplayName("달성하지 못한 업적 목록 조회 시 page 번호가 제대로 처리된다")
    @Test
    fun `달성하지 못한 업적 목록 조회 시 page 번호가 제대로 처리된다`() {
        //given
        val url = "$URI_PREFIX/not-achieved"
        val page = 1
        every { achievementQueryService.getNotAchievedAchievements(any(), any()) } returns Page.empty()

        //when
        mvc.perform(
            get(url)
                .queryParam("page", page.toString())
        )

        //then
        verify { achievementQueryService.getNotAchievedAchievements(any(), eq(page)) }
    }

}