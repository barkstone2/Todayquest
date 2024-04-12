package dailyquest.achivement.controller

import com.ninjasquad.springmockk.MockkBean
import dailyquest.achievement.dto.AchievementSaveRequest
import dailyquest.achievement.dto.AchievementUpdateRequest
import dailyquest.achievement.entity.Achievement
import dailyquest.achievement.entity.AchievementType
import dailyquest.achievement.repository.AchievementRepository
import dailyquest.common.BatchApiUtil
import dailyquest.context.IntegrationTestContext
import dailyquest.context.MockElasticsearchTestContextConfig
import dailyquest.context.MockRedisTestContextConfig
import dailyquest.jwt.JwtTokenProvider
import dailyquest.user.repository.UserRepository
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.data.repository.findByIdOrNull
import org.springframework.test.web.servlet.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.web.context.WebApplicationContext

@Import(MockElasticsearchTestContextConfig::class, MockRedisTestContextConfig::class)
@ExtendWith(MockKExtension::class)
@DisplayName("관리자 업적 API 컨트롤러 통합 테스트")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AdminAchievementApiControllerTest @Autowired constructor(
    context: WebApplicationContext,
    userRepository: UserRepository,
    jwtTokenProvider: JwtTokenProvider,
    private val achievementRepository: AchievementRepository,
): IntegrationTestContext(context, userRepository, jwtTokenProvider) {

    @MockkBean(relaxed = true)
    private lateinit var batchApiUtil: BatchApiUtil
    private val uriPrefix = "/admin/api/v1/achievements"

    @DisplayName("신규 업적 등록 시")
    @Nested
    inner class TestSaveAchievement {
        private val url = "$SERVER_ADDR$port$uriPrefix"
        private val saveRequest = AchievementSaveRequest("저장", "저장", AchievementType.QUEST_REGISTRATION, 1)

        @DisplayName("타입과 목표값이 모두 중복될 경우 400이 반환된다")
        @Test
        fun `타입과 목표값이 모두 중복될 경우 400이 반환된다`() {
            //given
            achievementRepository.save(saveRequest.mapToEntity())
            val requestBody = om.writeValueAsString(saveRequest)

            //when
            val result = mvc.perform {
                MockMvcRequestBuilders.post(url)
                    .useAdminConfiguration()
                    .content(requestBody)
                    .buildRequest(it)
            }

            //then
            result.andExpect { MockMvcResultMatchers.status().isBadRequest }
        }

        @DisplayName("타입과 목표값이 모두 중복될 경우 신규 업적이 등록되지 않는다")
        @Test
        fun `타입과 목표값이 모두 중복될 경우 신규 업적이 등록되지 않는다`() {
            //given
            achievementRepository.save(saveRequest.mapToEntity())
            val requestBody = om.writeValueAsString(saveRequest)

            //when
            mvc.perform {
                MockMvcRequestBuilders.post(url)
                    .useAdminConfiguration()
                    .content(requestBody)
                    .buildRequest(it)
            }

            //then
            val filteredResult = achievementRepository.findAll()
                .filter { it.type == saveRequest.type && it.targetValue == saveRequest.targetValue }
            assertThat(filteredResult.size).isOne()
        }

        @DisplayName("타입과 목표값이 중복되지 않으면 신규 업적이 등록된다")
        @Test
        fun `타입과 목표값이 중복되지 않으면 신규 업적이 등록된다`() {
            //given
            val requestBody = om.writeValueAsString(saveRequest)

            //when
            mvc.perform {
                MockMvcRequestBuilders.post(url)
                    .useAdminConfiguration()
                    .content(requestBody)
                    .buildRequest(it)
            }.andExpect { MockMvcResultMatchers.status().isOk }

            //then
            val allAchievements = achievementRepository.findAll()
            assertThat(allAchievements).anyMatch { it.type == saveRequest.type && it.targetValue == saveRequest.targetValue }
        }

        @DisplayName("신규 업적 등록 후 배치에 업적 달성 확인 요청을 보낸다")
        @Test
        fun `신규 업적 등록 후 배치에 업적 달성 확인 요청을 보낸다`() {
            //given
            val requestBody = om.writeValueAsString(saveRequest)

            //when
            mvc.perform {
                MockMvcRequestBuilders.post(url)
                    .useAdminConfiguration()
                    .content(requestBody)
                    .buildRequest(it)
            }

            //then
            verify { batchApiUtil.checkAndAchieve(any()) }
        }
    }

    @DisplayName("업적 변경 요청 시")
    @Nested
    inner class TestUpdateAchievement {
        private lateinit var achievement: Achievement

        @BeforeEach
        fun init() {
            achievement = Achievement("save", "save", AchievementType.QUEST_REGISTRATION, 1)
            achievementRepository.save(achievement)
        }

        @DisplayName("업적의 제목이 요청한 제목으로 변경된다")
        @Test
        fun `업적의 제목이 요청한 제목으로 변경된다`() {
            //given
            val url = "$uriPrefix/${achievement.id}"
            val updateTitle = "update"
            val updateRequest = AchievementUpdateRequest(updateTitle, "update")
            val requestBody = om.writeValueAsString(updateRequest)

            //when
            mvc.patch(url) {
                useAdminConfiguration()
                content = requestBody
            }

            //then
            val result = achievementRepository.findByIdOrNull(achievement.id)
            assertThat(result?.title).isEqualTo(updateTitle)
        }

        @DisplayName("업적의 설명이 요청한 설명으로 변경된다")
        @Test
        fun `업적의 설명이 요청한 설명으로 변경된다`() {
            //given
            val url = "$uriPrefix/${achievement.id}"
            val updateDescription = "update"
            val updateRequest = AchievementUpdateRequest("update", updateDescription)
            val requestBody = om.writeValueAsString(updateRequest)

            //when
            mvc.patch(url) {
                useAdminConfiguration()
                content = requestBody
            }

            //then
            val result = achievementRepository.findByIdOrNull(achievement.id)
            assertThat(result?.description).isEqualTo(updateDescription)
        }
    }

    @DisplayName("업적 비활성화 요청 시")
    @Nested
    inner class TestInactivateAchievement {
        private lateinit var achievement: Achievement

        @BeforeEach
        fun init() {
            achievement = Achievement("save", "save", AchievementType.QUEST_REGISTRATION, 1)
            achievementRepository.save(achievement)
        }

        @DisplayName("업적이 비활성화 상태로 변경된다")
        @Test
        fun `업적이 비활성화 상태로 변경된다`() {
            //given
            val beforeState = achievement.inactivated
            val url = "$uriPrefix/${achievement.id}/inactivate"

            //when
            mvc.patch(url) {
                useAdminConfiguration()
            }

            //then
            val result = achievementRepository.findByIdOrNull(achievement.id)
            assertThat(result?.inactivated).isNotEqualTo(beforeState).isTrue()
        }
    }

    @DisplayName("업적 활성화 요청 시")
    @Nested
    inner class TestActivateAchievement {
        private lateinit var achievement: Achievement

        @BeforeEach
        fun init() {
            achievement = Achievement("save", "save", AchievementType.QUEST_REGISTRATION, 1)
            achievement.inactivateAchievement()
            achievementRepository.save(achievement)
        }

        @DisplayName("업적이 활성화 상태로 변경된다")
        @Test
        fun `업적이 활성화 상태로 변경된다`() {
            //given
            val beforeState = achievement.inactivated
            val url = "$uriPrefix/${achievement.id}/activate"

            //when
            mvc.patch(url) {
                useAdminConfiguration()
            }

            //then
            val result = achievementRepository.findByIdOrNull(achievement.id)
            assertThat(result?.inactivated).isNotEqualTo(beforeState).isFalse()
        }
    }
}