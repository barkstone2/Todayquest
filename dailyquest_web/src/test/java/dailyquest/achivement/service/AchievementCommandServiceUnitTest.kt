package dailyquest.achivement.service

import dailyquest.achievement.dto.AchievementAchieveRequest
import dailyquest.achievement.dto.AchievementRequest
import dailyquest.achievement.entity.Achievement
import dailyquest.achievement.repository.AchievementRepository
import dailyquest.achievement.service.AchievementCommandService
import dailyquest.achievement.service.AchievementAchieveLogCommandService
import dailyquest.achievement.service.AchievementQueryService
import dailyquest.achievement.util.AchievementCurrentValueResolver
import dailyquest.common.MessageUtil
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
@DisplayName("업적 커맨드 서비스 단위 테스트")
class AchievementCommandServiceUnitTest {

    @InjectMockKs
    lateinit var achievementCommandService: AchievementCommandService
    @RelaxedMockK
    lateinit var achievementQueryService: AchievementQueryService
    @RelaxedMockK
    lateinit var achieveLogCommandService: AchievementAchieveLogCommandService
    @RelaxedMockK
    lateinit var achievementCurrentValueResolver: AchievementCurrentValueResolver
    @RelaxedMockK
    lateinit var achievementRepository: AchievementRepository

    @DisplayName("업적 확인 후 달성 요청 시")
    @Nested
    inner class TestCheckAndAchieveAchievements {

        @RelaxedMockK
        private lateinit var targetAchievement: Achievement

        @RelaxedMockK
        private lateinit var achieveRequest: AchievementAchieveRequest

        private val savedAchievements = mutableListOf<Achievement>()

        @BeforeEach
        fun init() {
            savedAchievements.clear()
            every { achievementQueryService.getNotAchievedAchievement(any(), any()) } returns targetAchievement
            every { targetAchievement.canAchieve(any()) } returns true
        }

        @DisplayName("업적 달성 가능 여부가 true면 achieve가 호출된다")
        @Test
        fun `업적 달성 가능 여부가 true면 achieve가 호출된다`() {
            //given
            every { targetAchievement.canAchieve(any()) } returns true

            //when
            achievementCommandService.checkAndAchieveAchievement(achieveRequest)

            //then
            verify { achieveLogCommandService.achieve(eq(targetAchievement), any()) }
        }

        @DisplayName("업적 달성 가능 여부가 false면 achieve가 호출되지 않는다")
        @Test
        fun `업적 달성 가능 여부가 false면 achieve가 호출되지 않는다`() {
            //given
            every { targetAchievement.canAchieve(any()) } returns false

            //when
            achievementCommandService.checkAndAchieveAchievement(achieveRequest)

            //then
            verify { achieveLogCommandService wasNot Called }
        }
    }

    @DisplayName("업적 신규 등록 시")
    @Nested
    inner class TestSaveAchievement {

        @RelaxedMockK
        private lateinit var saveRequest: AchievementRequest

        @BeforeEach
        fun init() {
            mockkStatic(MessageUtil::class)
            every { MessageUtil.getMessage(any()) } returns ""
            every { achievementRepository.save(any()) } answers { nothing }
        }

        @DisplayName("타입과 목표값이 모두 동일한 업적이 있다면 예외를 던진다")
        @Test
        fun `타입과 목표값이 모두 동일한 업적이 있다면 예외를 던진다`() {
            //given
            every { achievementRepository.existsByTypeAndTargetValue(any(), any()) } returns true

            //when
            //then
            assertThrows<IllegalStateException> { achievementCommandService.saveAchievement(saveRequest) }
        }

        @DisplayName("타입과 목표값이 모두 동일한 업적이 없다면 업적을 저장한다")
        @Test
        fun `타입과 목표값이 모두 동일한 업적이 없다면 업적을 저장한다`() {
            //given
            every { achievementRepository.existsByTypeAndTargetValue(any(), any()) } returns false

            //when
            achievementCommandService.saveAchievement(saveRequest)

            //then
            verify { achievementRepository.save(any()) }
        }
    }
}