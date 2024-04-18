package dailyquest.achivement.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import dailyquest.achievement.controller.AdminAchievementApiController
import dailyquest.achievement.dto.AchievementSaveRequest
import dailyquest.achievement.dto.AchievementUpdateRequest
import dailyquest.achievement.entity.AchievementType
import dailyquest.achievement.service.AchievementService
import dailyquest.annotation.WebMvcUnitTest
import dailyquest.common.BatchApiUtil
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.patch
import org.springframework.test.web.servlet.post

@ExtendWith(MockKExtension::class)
@WebMvcUnitTest([AdminAchievementApiController::class])
@DisplayName("관리자 업적 API 컨트롤러 유닛 테스트")
class AdminAchievementApiControllerUnitTest @Autowired constructor(
    private val mvc: MockMvc,
    private val om: ObjectMapper
) {
    @MockkBean(relaxed = true)
    private lateinit var achievementService: AchievementService
    @MockkBean(relaxed = true)
    private lateinit var batchApiUtil: BatchApiUtil
    private val urlPrefix = "/admin/api/v1/achievements"

    @DisplayName("업적 등록 요청 시")
    @Nested
    inner class TestSaveAchievement {
        @DisplayName("업적 제목이 50자를 초과하면 400이 반환된다")
        @Test
        fun `업적 제목이 50자를 초과하면 400이 반환된다`() {
            //given
            val exceedTitle = "t".repeat(51)
            val saveRequest = AchievementSaveRequest(exceedTitle, "description", AchievementType.QUEST_REGISTRATION, 1)

            //when
            val result = mvc.post(urlPrefix) {
                this.content = om.writeValueAsString(saveRequest)
                this.contentType = MediaType.APPLICATION_JSON
                this.with(csrf())
            }

            //then
            result.andExpect { status { HttpStatus.BAD_REQUEST } }
        }

        @DisplayName("업적 제목이 공백이면 400이 반환된다")
        @Test
        fun `업적 제목이 공백이면 400이 반환된다`() {
            //given
            val emptyTitle = " "
            val saveRequest = AchievementSaveRequest(emptyTitle, "description", AchievementType.QUEST_REGISTRATION, 1)

            //when
            val result = mvc.post(urlPrefix) {
                this.content = om.writeValueAsString(saveRequest)
                this.contentType = MediaType.APPLICATION_JSON
                this.with(csrf())
            }

            //then
            result.andExpect { status { HttpStatus.BAD_REQUEST } }
        }

        @DisplayName("업적 설명이 150자를 초과하면 400이 반환된다")
        @Test
        fun `업적 설명이 150자를 초과하면 400이 반환된다`() {
            //given
            val exceedDescription = "d".repeat(151)
            val saveRequest = AchievementSaveRequest("title", exceedDescription, AchievementType.QUEST_REGISTRATION, 1)

            //when
            val result = mvc.post(urlPrefix) {
                this.content = om.writeValueAsString(saveRequest)
                this.contentType = MediaType.APPLICATION_JSON
                this.with(csrf())
            }

            //then
            result.andExpect { status { HttpStatus.BAD_REQUEST } }
        }

        @DisplayName("업적 설명이 공백이면 400이 반환된다")
        @Test
        fun `업적 설명이 공백이면 400이 반환된다`() {
            // given
            val emptyDescription = " "
            val saveRequest = AchievementSaveRequest("title", emptyDescription, AchievementType.QUEST_REGISTRATION, 1)

            //when
            val result = mvc.post(urlPrefix) {
                this.content = om.writeValueAsString(saveRequest)
                this.contentType = MediaType.APPLICATION_JSON
                this.with(csrf())
            }

            //then
            result.andExpect { status { HttpStatus.BAD_REQUEST } }
        }
        
        @DisplayName("업적 목표값이 0이면 400이 반환된다")
        @Test
        fun `업적 목표값이 0이면 400이 반환된다`() {
            // given
            val zeroTargetValue = 0L
            val saveRequest = AchievementSaveRequest("title", "description", AchievementType.QUEST_REGISTRATION, zeroTargetValue)

            //when
            val result = mvc.post(urlPrefix) {
                this.content = om.writeValueAsString(saveRequest)
                this.contentType = MediaType.APPLICATION_JSON
                this.with(csrf())
            }

            //then
            result.andExpect { status { HttpStatus.BAD_REQUEST } }
        }

        @DisplayName("Validation에 성공하면 업적 등록을 요청하고, 배치에 업적 달성 확인 요청을 한다")
        @Test
        fun `Validation에 성공하면 업적 등록을 요청하고, 배치에 업적 달성 확인 요청을 한다`() {
            // given
            val saveRequest = AchievementSaveRequest("title", "description", AchievementType.QUEST_REGISTRATION, 1L)

            //when
            mvc.post(urlPrefix) {
                this.content = om.writeValueAsString(saveRequest)
                this.contentType = MediaType.APPLICATION_JSON
                this.with(csrf())
            }.andExpect { status { HttpStatus.OK } }

            //then
            verify {
                achievementService.saveAchievement(any())
                batchApiUtil.checkAndAchieve(any())
            }
        }
    }

    @DisplayName("업적 수정 요청 시")
    @Nested
    inner class TestUpdateAchievement {
        private val url = "$urlPrefix/1"
        @DisplayName("업적 제목이 50자를 초과하면 400이 반환된다")
        @Test
        fun `업적 제목이 50자를 초과하면 400이 반환된다`() {
            //given
            val exceedTitle = "t".repeat(51)
            val updateRequest = AchievementUpdateRequest(exceedTitle, "description")

            //when
            val result = mvc.patch(url) {
                this.content = om.writeValueAsString(updateRequest)
                this.contentType = MediaType.APPLICATION_JSON
                this.with(csrf())
            }

            //then
            result.andExpect { status { HttpStatus.BAD_REQUEST } }
        }

        @DisplayName("업적 제목이 공백이면 400이 반환된다")
        @Test
        fun `업적 제목이 공백이면 400이 반환된다`() {
            //given
            val emptyTitle = " "
            val updateRequest = AchievementUpdateRequest(emptyTitle, "description")

            //when
            val result = mvc.patch(url) {
                this.content = om.writeValueAsString(updateRequest)
                this.contentType = MediaType.APPLICATION_JSON
                this.with(csrf())
            }

            //then
            result.andExpect { status { HttpStatus.BAD_REQUEST } }
        }

        @DisplayName("업적 설명이 150자를 초과하면 400이 반환된다")
        @Test
        fun `업적 설명이 150자를 초과하면 400이 반환된다`() {
            //given
            val exceedDescription = "d".repeat(151)
            val updateRequest = AchievementUpdateRequest("title", exceedDescription)

            //when
            val result = mvc.patch(url) {
                this.content = om.writeValueAsString(updateRequest)
                this.contentType = MediaType.APPLICATION_JSON
                this.with(csrf())
            }

            //then
            result.andExpect { status { HttpStatus.BAD_REQUEST } }
        }

        @DisplayName("업적 설명이 공백이면 400이 반환된다")
        @Test
        fun `업적 설명이 공백이면 400이 반환된다`() {
            // given
            val emptyDescription = " "
            val updateRequest = AchievementUpdateRequest("title", emptyDescription)

            //when
            val result = mvc.patch(url) {
                this.content = om.writeValueAsString(updateRequest)
                this.contentType = MediaType.APPLICATION_JSON
                this.with(csrf())
            }

            //then
            result.andExpect { status { HttpStatus.BAD_REQUEST } }
        }

        @DisplayName("Validation에 성공하면 업적 등록을 요청하고, 배치에 업적 달성 확인 요청을 한다")
        @Test
        fun `Validation에 성공하면 업적 등록을 요청하고, 배치에 업적 달성 확인 요청을 한다`() {
            // given
            val updateRequest = AchievementUpdateRequest("title", "description")

            //when
            mvc.patch(url) {
                this.content = om.writeValueAsString(updateRequest)
                this.contentType = MediaType.APPLICATION_JSON
                this.with(csrf())
            }.andExpect { status { HttpStatus.OK } }

            //then
            verify {
                achievementService.updateAchievement(any(), any())
            }
        }
    }

    @DisplayName("업적 비활성화 요청 시")
    @Nested
    inner class TestInactivateAchievement {
        @DisplayName("요청 URL의 ID에 대해 비활성화 요청한다")
        @Test
        fun `요청 URL의 ID에 대해 비활성화 요청한다`() {
            // given
            val achievementId = 1L
            val url = "$urlPrefix/$achievementId/inactivate"

            //when
            mvc.patch(url) {
                this.with(csrf())
            }

            //then
            verify {
                achievementService.inactivateAchievement(eq(achievementId))
            }
        }
    }

    @DisplayName("업적 활성화 요청 시")
    @Nested
    inner class TestActivateAchievement {
        @DisplayName("요청 URL의 ID에 대해 활성화 요청한다")
        @Test
        fun `요청 URL의 ID에 대해 활성화 요청한다`() {
            // given
            val achievementId = 1L
            val url = "$urlPrefix/$achievementId/activate"

            //when
            mvc.patch(url) {
                this.with(csrf())
            }

            //then
            verify {
                achievementService.activateAchievement(eq(achievementId))
            }
        }
    }

    @DisplayName("모든 업적과 업적 타입 조회 시")
    @Nested
    inner class TestGetAllAchievementsAndType {
        private val url = urlPrefix

        @DisplayName("타입으로 그룹핑된 업적 정보를 조회를 요청한다")
        @Test
        fun `타입으로 그룹핑된 업적 정보를 조회를 요청한다`() {
            //given
            //when
            mvc.get(url)

            //then
            verify { achievementService.getAllAchievementsGroupByType() }
        }

        @DisplayName("반환된 응답에 모든 타입 정보가 포함되어 있다")
        @Test
        fun `반환된 응답에 모든 타입 정보가 포함되어 있다`() {
            //given
            val achievementTypes = AchievementType.values().map { it.name }

            //when
            val result = mvc.get(url)

            //then
            result.andExpect {
                jsonPath("$.data.achievementTypes.*.key") {
                    value(equalTo(achievementTypes))
                }
            }
        }
    }
}