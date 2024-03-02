package dailyquest.admin.controller

import com.fasterxml.jackson.core.type.TypeReference
import dailyquest.achievement.dto.AchievementRequest
import dailyquest.achievement.entity.AchievementType
import dailyquest.achievement.repository.AchievementRepository
import dailyquest.admin.dto.SystemSettingsRequest
import dailyquest.admin.dto.SystemSettingsResponse
import dailyquest.admin.service.AdminService
import dailyquest.common.ResponseData
import dailyquest.context.IntegrationTestContextWithRedis
import dailyquest.jwt.JwtTokenProvider
import dailyquest.user.repository.UserRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.context.WebApplicationContext

@DisplayName("관리자 API 컨트롤러 통합 테스트")
@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AdminApiControllerTest @Autowired constructor(
    context: WebApplicationContext,
    userRepository: UserRepository,
    jwtTokenProvider: JwtTokenProvider,
    private val adminService: AdminService,
    private val achievementRepository: AchievementRepository,
): IntegrationTestContextWithRedis(context, userRepository, jwtTokenProvider) {

    private val uriPrefix = "/admin/api/v1"

    @DisplayName("시스템 설정 조회 시")
    @Nested
    inner class GetSystemSettingsTest {

        private val url = "$SERVER_ADDR$port$uriPrefix/reward"

        @DisplayName("관리자 권한이 아니면 FORBIDDEN이 반환된다")
        @Test
        fun `관리자 권한이 아니면 FORBIDDEN이 반환된다`() {
            //given
            //when
            val request = mvc
                .perform(
                    get(url)
                        .useUserConfiguration()
                )

            //then
            request.andExpect(status().isForbidden)
        }

        @DisplayName("관리자 권한이라면 시스템 세팅이 반환된다")
        @Test
        fun `관리자 권한이라면 시스템 세팅이 반환된다`() {
            //given
            //when
            val request = mvc
                .perform(
                    get(url)
                        .useAdminConfiguration()
                )

            //then
            val body = request
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn()
                .response
                .contentAsString

            om.readValue(body, object: TypeReference<ResponseData<SystemSettingsResponse>>(){})
        }
    }

    @DisplayName("시스템 설정 변경 시")
    @Nested
    inner class UpdateSystemSettingsTest {
        private val url = "$SERVER_ADDR$port$uriPrefix/reward"

        @DisplayName("관리자 권한이 아니면 FORBIDDEN이 반환된다")
        @Test
        fun `관리자 권한이 아니면 FORBIDDEN이 반환된다`() {
            //given
            val requestDto = SystemSettingsRequest(5, 5, 10)
            val requestBody = om.writeValueAsString(requestDto)

            //when
            val request = mvc
                .perform(
                    put(url)
                        .useUserConfiguration()
                        .content(requestBody)
                )

            //then
            request.andExpect(status().isForbidden)
        }

        @DisplayName("관리자 권한이라면 시스템 세팅이 변경된다")
        @Test
        fun `관리자 권한이라면 시스템 세팅이 변경된다`() {
            //given
            val requestDto = SystemSettingsRequest(5, 5, 10)
            val requestBody = om.writeValueAsString(requestDto)

            //when
            val request = mvc
                .perform(
                    put(url)
                        .useAdminConfiguration()
                        .content(requestBody)
                )

            //then
            request.andExpect(status().isOk)

            val systemSettings = adminService.getSystemSettings()

            assertThat(systemSettings.maxRewardCount).isEqualTo(requestDto.maxRewardCount)
            assertThat(systemSettings.questClearExp).isEqualTo(requestDto.questClearExp)
            assertThat(systemSettings.questClearGold).isEqualTo(requestDto.questClearGold)
        }
    }

    @DisplayName("경험치 테이블 조회 시")
    @Nested
    inner class GetExpTableTest {
        private val url = "$SERVER_ADDR$port$uriPrefix/exp-table"

        @DisplayName("관리자 권한이 아니면 FORBIDDEN이 반환된다")
        @Test
        fun `관리자 권한이 아니면 FORBIDDEN이 반환된다`() {
            //given
            //when
            val request = mvc
                .perform(
                    get(url)
                        .useUserConfiguration()
                )

            //then
            request.andExpect(status().isForbidden)
        }

        @DisplayName("관리자 권한이라면 경험치 테이블이 반환된다")
        @Test
        fun `관리자 권한이라면 경험치 테이블이 반환된다`() {
            //given
            //when
            val request = mvc
                .perform(
                    get(url)
                        .useAdminConfiguration()
                )

            //then
            val body = request
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn()
                .response
                .contentAsString

            om.readValue(body, object: TypeReference<ResponseData<Map<String, Long>>>(){})
        }
    }


    @DisplayName("경험치 테이블 변경 시")
    @Nested
    inner class UpdateExpTableTest {
        private val url = "$SERVER_ADDR$port$uriPrefix/exp-table"

        @DisplayName("관리자 권한이 아니면 FORBIDDEN이 반환된다")
        @Test
        fun `관리자 권한이 아니면 FORBIDDEN이 반환된다`() {
            //given
            val requestMap = mapOf(1 to 5, 2 to 5, 3 to 10)
            val requestBody = om.writeValueAsString(requestMap)

            //when
            val request = mvc
                .perform(
                    put(url)
                        .useUserConfiguration()
                        .content(requestBody)
                )

            //then
            request
                .andExpect(status().isForbidden)
        }

        @DisplayName("관리자 권한이라면 경험치 테이블이 변경된다")
        @Test
        fun `관리자 권한이라면 경험치 테이블이 변경된다`() {
            //given
            val requestMap = mapOf(1 to 5L, 2 to 5L, 3 to 10L)
            val requestBody = om.writeValueAsString(requestMap)

            //when
            val request = mvc
                .perform(
                    put(url)
                        .useAdminConfiguration()
                        .content(requestBody)
                )

            //then
            request.andExpect(status().isOk)

            val expTable = adminService.getExpTable()

            assertThat(expTable[1]).isEqualTo(requestMap[1])
            assertThat(expTable[2]).isEqualTo(requestMap[2])
            assertThat(expTable[3]).isEqualTo(requestMap[3])
        }
    }

    @DisplayName("신규 업적 등록 시")
    @Nested
    inner class TestSaveAchievement {
        private val url = "$SERVER_ADDR$port$uriPrefix/achievements"
        private val saveRequest = AchievementRequest("저장", "저장", AchievementType.USER_LEVEL, 1)

        @DisplayName("타입과 목표값이 모두 중복될 경우 400이 반환된다")
        @Test
        fun `타입과 목표값이 모두 중복될 경우 400이 반환된다`() {
            //given
            achievementRepository.save(saveRequest.mapToEntity())
            val requestBody = om.writeValueAsString(saveRequest)

            //when
            val result = mvc.perform {
                post(url)
                    .useAdminConfiguration()
                    .content(requestBody)
                    .buildRequest(it)
            }

            //then
            result.andExpect { status().isBadRequest }
        }

        @DisplayName("타입과 목표값이 모두 중복될 경우 신규 업적이 등록되지 않는다")
        @Test
        fun `타입과 목표값이 모두 중복될 경우 신규 업적이 등록되지 않는다`() {
            //given
            achievementRepository.save(saveRequest.mapToEntity())
            val requestBody = om.writeValueAsString(saveRequest)

            //when
            mvc.perform {
                post(url)
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
                post(url)
                    .useAdminConfiguration()
                    .content(requestBody)
                    .buildRequest(it)
            }.andExpect { status().isOk }

            //then
            val allAchievements = achievementRepository.findAll()
            assertThat(allAchievements).anyMatch { it.type == saveRequest.type && it.targetValue == saveRequest.targetValue }
        }
    }
}