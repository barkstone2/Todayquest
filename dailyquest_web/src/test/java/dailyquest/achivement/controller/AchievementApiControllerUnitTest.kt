package dailyquest.achivement.controller

import com.ninjasquad.springmockk.MockkBean
import dailyquest.achievement.controller.AchievementApiController
import dailyquest.achievement.entity.AchievementType
import dailyquest.achievement.service.AchievementQueryService
import dailyquest.annotation.WithCustomMockUser
import dailyquest.config.SecurityConfig
import dailyquest.jwt.JwtAuthorizationFilter
import io.mockk.verify
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get

@DisplayName("업적 API 컨트롤러 유닛 테스트")
@WithCustomMockUser
@WebMvcTest(controllers = [AchievementApiController::class],
    excludeFilters = [
        ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = [SecurityConfig::class, JwtAuthorizationFilter::class]
        )
    ]
)
class AchievementApiControllerUnitTest {
    companion object {
        const val URI_PREFIX = "/api/v1/achievements"
    }

    @Autowired
    lateinit var mvc: MockMvc

    @MockkBean(relaxed = true)
    lateinit var achievementQueryService: AchievementQueryService

    @DisplayName("type 쿼리 파라미터가 제대로 처리된다")
    @Test
    fun `type 쿼리 파라미터가 제대로 처리된다`() {
        //given
        val type = AchievementType.QUEST_REGISTRATION

        //when
        mvc.perform(
            get(URI_PREFIX)
                .queryParam("achievementType", type.name)
        )

        //then
        verify { achievementQueryService.getAchievementsWithAchieveInfo(eq(type), any()) }
    }

}