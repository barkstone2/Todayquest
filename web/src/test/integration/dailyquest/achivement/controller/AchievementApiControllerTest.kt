package dailyquest.achivement.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.SpykBean
import dailyquest.achievement.entity.Achievement
import dailyquest.achievement.entity.AchievementAchieveLog
import dailyquest.achievement.entity.AchievementType
import dailyquest.achievement.repository.AchievementAchieveLogRepository
import dailyquest.achievement.repository.AchievementRepository
import dailyquest.achievement.service.AchievementService
import dailyquest.context.IntegrationTestContext
import dailyquest.context.MockElasticsearchTestContextConfig
import dailyquest.context.MockRedisTestContextConfig
import dailyquest.jwt.JwtTokenProvider
import dailyquest.properties.AchievementPageSizeProperties
import dailyquest.user.repository.UserRepository
import io.mockk.every
import io.mockk.junit5.MockKExtension
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.web.servlet.get
import org.springframework.web.context.WebApplicationContext

@Import(MockElasticsearchTestContextConfig::class, MockRedisTestContextConfig::class)
@ExtendWith(MockKExtension::class)
@DisplayName("업적 API 컨트롤러 통합 테스트")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AchievementApiControllerTest @Autowired constructor(
    context: WebApplicationContext,
    userRepository: UserRepository,
    jwtTokenProvider: JwtTokenProvider,
    private val achievementService: AchievementService,
    private val achievementRepository: AchievementRepository,
    private val achievementAchieveLogRepository: AchievementAchieveLogRepository,
    private val om: ObjectMapper,
    @SpykBean
    private val achievementPageSizeProperties: AchievementPageSizeProperties
) : IntegrationTestContext(context, userRepository, jwtTokenProvider) {
    private val uriPrefix = "/api/v1/achievements"


    @DisplayName("달성한 업적 조회 시")
    @Nested
    inner class TestGetAchievedAchievements {
        private val url = "$uriPrefix/achieved"

        @DisplayName("달성하지 않은 업적은 조회되지 않는다")
        @Test
        fun `달성하지 않은 업적은 조회되지 않는다`() {
            //given
            val notAchievedAchievement = Achievement("t", "d", AchievementType.QUEST_REGISTRATION, 1)
            achievementRepository.save(notAchievedAchievement)

            //when
            val result = mvc.get(url) {
                useUserConfiguration()
            }

            //then
            result.andExpect {
                status { isOk() }
                jsonPath("$.data.content.*.id") {
                    value(not(contains(notAchievedAchievement.id.toInt())))
                }
            }
        }

        @DisplayName("다른 유저가 달성한 업적은 조회되지 않는다")
        @Test
        fun `다른 유저가 달성한 업적은 조회되지 않는다`() {
            //given
            val anotherUserId = anotherUser.id
            val anotherUserAchievedAchievement = Achievement("t", "d", AchievementType.QUEST_REGISTRATION, 1)
            achievementRepository.save(anotherUserAchievedAchievement)
            val achievementAchieveLog = AchievementAchieveLog(anotherUserAchievedAchievement, anotherUserId)
            achievementAchieveLogRepository.save(achievementAchieveLog)

            //when
            val result = mvc.get(url) {
                useUserConfiguration()
            }

            //then
            result.andExpect {
                status { isOk() }
                jsonPath("$.data.content.*.id") {
                    value(not(contains(anotherUserAchievedAchievement.id.toInt())))
                }
            }
        }

        @DisplayName("현재 유저가 달성한 업적이 조회된다")
        @Test
        fun `현재 유저가 달성한 업적이 조회된다`() {
            //given
            val userId = user.id
            val achievedAchievement = Achievement("t", "d", AchievementType.QUEST_REGISTRATION, 1)
            achievementRepository.save(achievedAchievement)
            val achievementAchieveLog = AchievementAchieveLog(achievedAchievement, userId)
            achievementAchieveLogRepository.save(achievementAchieveLog)

            //when
            val result = mvc.get(url) {
                useUserConfiguration()
            }

            //then
            result.andExpect {
                status { isOk() }
                jsonPath("$.data.content.*.id") {
                    value(contains(achievedAchievement.id.toInt()))
                }
            }
        }

        @DisplayName("요청한 페이지의 데이터만 조회된다")
        @Test
        fun `요청한 페이지의 데이터만 조회된다`() {
            //given
            val pageNo = 0
            val idx = 2 - pageNo
            val userId = user.id
            val achievements = listOf(
                achievementRepository.save(Achievement("t", "d", AchievementType.QUEST_REGISTRATION, 1)),
                achievementRepository.save(Achievement("t", "d", AchievementType.QUEST_REGISTRATION, 2)),
                achievementRepository.save(Achievement("t", "d", AchievementType.QUEST_REGISTRATION, 3)),
            )
            achievements.forEach {
                achievementAchieveLogRepository.save(AchievementAchieveLog(it, userId))
            }
            every { achievementPageSizeProperties.size } returns 1

            //when
            val result = mvc.get("$url?page=$pageNo") {
                useUserConfiguration()
            }

            //then
            result.andExpect {
                status { isOk() }
                jsonPath("$.data.content.size()") { value(1) }
                jsonPath("$.data.content.*.id") {
                    value(contains(achievements[idx].id.toInt()))
                }
            }
        }

        @DisplayName("비활성화 상태의 업적은 조회되지 않는다")
        @Test
        fun `비활성화 상태의 업적은 조회되지 않는다`() {
            //given
            val userId = user.id
            val inactivatedAchievement = Achievement("t", "d", AchievementType.QUEST_REGISTRATION, 1)
            inactivatedAchievement.inactivateAchievement()
            achievementRepository.save(inactivatedAchievement)
            val achievementAchieveLog = AchievementAchieveLog(inactivatedAchievement, userId)
            achievementAchieveLogRepository.save(achievementAchieveLog)

            //when
            val result = mvc.get(url) {
                useUserConfiguration()
            }

            //then
            result.andExpect {
                status { isOk() }
                jsonPath("$.data.content.*.id") {
                    value(not(contains(inactivatedAchievement.id.toInt())))
                }
            }
        }

        @DisplayName("업적 달성일이 최근인 순으로 조회된다")
        @Test
        fun `업적 달성일이 최근인 순으로 조회된다`() {
            //given
            val userId = user.id
            val achievements = listOf(
                achievementRepository.save(Achievement("t", "d", AchievementType.QUEST_REGISTRATION, 1)),
                achievementRepository.save(Achievement("t", "d", AchievementType.QUEST_REGISTRATION, 2)),
                achievementRepository.save(Achievement("t", "d", AchievementType.QUEST_REGISTRATION, 3)),
            )
            achievements.forEach {
                achievementAchieveLogRepository.save(AchievementAchieveLog(it, userId))
            }

            //when
            val result = mvc.get(url) {
                useUserConfiguration()
            }

            //then
            result.andExpect {
                status { isOk() }
                jsonPath("$.data.content.*.id") {
                    value(equalTo(achievements.reversed().map { it.id }.toTypedArray()))
                }
            }
        }
    }

    @DisplayName("달성하지 못한 업적 조회 시")
    @Nested
    inner class TestGetNotAchievedAchievements {
        private val url = "$uriPrefix/not-achieved"

        @DisplayName("달성한 업적은 조회되지 않는다")
        @Test
        fun `달성한 업적은 조회되지 않는다`() {
            //given
            val userId = user.id
            val achievedAchievement = Achievement("t", "d", AchievementType.QUEST_REGISTRATION, 1)
            achievementRepository.save(achievedAchievement)
            val achievementAchieveLog = AchievementAchieveLog(achievedAchievement, userId)
            achievementAchieveLogRepository.save(achievementAchieveLog)

            //when
            val result = mvc.get(url) {
                useUserConfiguration()
            }

            //then
            result.andExpect {
                status { isOk() }
                jsonPath("$.data.content.*.id") {
                    value(not(contains(achievedAchievement.id.toInt())))
                }
            }
        }

        @DisplayName("다른 유저가 달성한 업적은 조회된다")
        @Test
        fun `다른 유저가 달성한 업적은 조회된다`() {
            //given
            val anotherUserId = anotherUser.id
            val anotherUserAchievedAchievement = Achievement("t", "d", AchievementType.QUEST_REGISTRATION, 1)
            achievementRepository.save(anotherUserAchievedAchievement)
            val achievementAchieveLog = AchievementAchieveLog(anotherUserAchievedAchievement, anotherUserId)
            achievementAchieveLogRepository.save(achievementAchieveLog)

            //when
            val result = mvc.get(url) {
                useUserConfiguration()
            }

            //then
            result.andExpect {
                status { isOk() }
                jsonPath("$.data.content.*.id") {
                    value(contains(anotherUserAchievedAchievement.id.toInt()))
                }
            }
        }

        @DisplayName("현재 유저가 달성하지 않은 업적이 조회된다")
        @Test
        fun `현재 유저가 달성하지 않은 업적이 조회된다`() {
            //given
            val notAchievedAchievement = Achievement("t", "d", AchievementType.QUEST_REGISTRATION, 1)
            achievementRepository.save(notAchievedAchievement)

            //when
            val result = mvc.get(url) {
                useUserConfiguration()
            }

            //then
            result.andExpect {
                status { isOk() }
                jsonPath("$.data.content.*.id") {
                    value(contains(notAchievedAchievement.id.toInt()))
                }
            }
        }

        @DisplayName("요청한 페이지의 데이터만 조회된다")
        @Test
        fun `요청한 페이지의 데이터만 조회된다`() {
            //given
            val pageNo = 0
            val achievements = listOf(
                achievementRepository.save(Achievement("t", "d", AchievementType.QUEST_REGISTRATION, 1)),
                achievementRepository.save(Achievement("t", "d", AchievementType.QUEST_REGISTRATION, 2)),
                achievementRepository.save(Achievement("t", "d", AchievementType.QUEST_REGISTRATION, 3)),
            )
            every { achievementPageSizeProperties.size } returns 1

            //when
            val result = mvc.get(url) {
                useUserConfiguration()
                param("page", pageNo.toString())
            }

            //then
            result.andExpect {
                status { isOk() }
                jsonPath("$.data.content.size()") { value(1) }
                jsonPath("$.data.content.*.id") {
                    value(contains(achievements[pageNo].id.toInt()))
                }
            }
        }

        @DisplayName("비활성화 상태의 업적은 조회되지 않는다")
        @Test
        fun `비활성화 상태의 업적은 조회되지 않는다`() {
            //given
            val inactivatedAchievement = Achievement("t", "d", AchievementType.QUEST_REGISTRATION, 1)
            inactivatedAchievement.inactivateAchievement()
            achievementRepository.save(inactivatedAchievement)

            //when
            val result = mvc.get(url) {
                useUserConfiguration()
            }

            //then
            result.andExpect {
                status { isOk() }
                jsonPath("$.data.content.*.id") {
                    value(not(contains(inactivatedAchievement.id.toInt())))
                }
            }
        }

        @DisplayName("목표값이 낮은 순으로 조회된다")
        @Test
        fun `목표값이 낮은 순으로 조회된다`() {
            //given
            val achievements = listOf(
                achievementRepository.save(Achievement("t", "d", AchievementType.QUEST_REGISTRATION, 1)),
                achievementRepository.save(Achievement("t", "d", AchievementType.QUEST_REGISTRATION, 2)),
                achievementRepository.save(Achievement("t", "d", AchievementType.QUEST_REGISTRATION, 3)),
            )

            //when
            val result = mvc.get(url) {
                useUserConfiguration()
            }

            //then
            result.andExpect {
                status { isOk() }
                jsonPath("$.data.content.*.id") {
                    value(equalTo(achievements.map { it.id }.toTypedArray()))
                }
            }
        }
    }
}