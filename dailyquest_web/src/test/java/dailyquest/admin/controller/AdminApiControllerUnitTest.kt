package dailyquest.admin.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import dailyquest.achievement.dto.AchievementRequest
import dailyquest.achievement.entity.AchievementType
import dailyquest.achievement.service.AchievementCommandService
import dailyquest.admin.service.AdminService
import dailyquest.annotation.WithCustomMockUser
import dailyquest.common.BatchApiUtil
import dailyquest.config.SecurityConfig
import dailyquest.filter.InternalApiKeyValidationFilter
import dailyquest.jwt.JwtAuthorizationFilter
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post

@ExtendWith(MockKExtension::class)
@WithCustomMockUser
@WebMvcTest(
    controllers = [AdminApiController::class],
    excludeFilters = [
        ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = [SecurityConfig::class, JwtAuthorizationFilter::class, InternalApiKeyValidationFilter::class]
        )
    ]
)
@DisplayName("관리자 API 컨트롤러 유닛 테스트")
class AdminApiControllerUnitTest {
    @MockkBean(relaxed = true)
    private lateinit var adminService: AdminService

    @MockkBean(relaxed = true)
    private lateinit var achievementCommandService: AchievementCommandService

    @MockkBean(relaxed = true)
    private lateinit var batchApiUtil: BatchApiUtil

    @Autowired
    private lateinit var mvc: MockMvc

    @Autowired
    private lateinit var om: ObjectMapper
    private val urlPrefix = "/admin/api/v1"

    @DisplayName("업적 등록 시")
    @Nested
    inner class TestSaveAchievement {
        @DisplayName("업적 등록 후 배치 애플리케이션에 처리 요청을 보낸다")
        @Test
        fun `업적 등록 후 배치 애플리케이션에 처리 요청을 보낸다`() {
            //given
            val url = "$urlPrefix/achievements"
            val saveRequest = AchievementRequest("", "", AchievementType.QUEST_REGISTRATION, 1)
            val achievementId = 1L
            every { achievementCommandService.saveAchievement(any()) } returns achievementId

            //when
            mvc.post(url) {
                this.with(csrf())
                this.contentType = MediaType.APPLICATION_JSON
                this.content = om.writeValueAsString(saveRequest)
            }

            //then
            verify { batchApiUtil.checkAndAchieve(eq(achievementId)) }
        }
    }
}