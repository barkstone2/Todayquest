package dailyquest.achivement.service

import dailyquest.achievement.dto.AchievementAchieveRequest
import dailyquest.achievement.entity.Achievement
import dailyquest.achievement.entity.AchievementType
import dailyquest.achievement.service.AchievementLogService
import dailyquest.achievement.service.AchievementQueryService
import dailyquest.achievement.service.AchievementService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.lenient
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify

@ExtendWith(MockitoExtension::class)
@DisplayName("업적 서비스 단위 테스트")
class AchievementServiceUnitTest {

    @Mock
    lateinit var achievementQueryService: AchievementQueryService
    @Mock
    lateinit var achievementLogService: AchievementLogService
    @InjectMocks
    lateinit var achievementService: AchievementService

    @DisplayName("업적 확인 후 달성 요청 시")
    @Nested
    inner class TestCheckAndAchieveAchievements {

        private val savedAchievements = mutableListOf<Achievement>()

        @BeforeEach
        fun init() {
            savedAchievements.clear()
            lenient().doAnswer { savedAchievements.add(it.getArgument(0)) }.`when`(achievementLogService).achieve(any(), any())
        }

        @DisplayName("요청 타입과 같은 타입의 업적 조회를 요청한다")
        @Test
        fun `요청 타입과 같은 타입의 업적 조회를 요청한다`() {
            //given
            val achievementType = AchievementType.QUEST_REGISTRATION
            val achieveRequest = AchievementAchieveRequest(achievementType, 1, 1L)

            //when
            achievementService.checkAndAchieveAchievements(achieveRequest)

            //then
            verify(achievementQueryService).getNotAchievedAchievement(eq(achievementType), eq(1L))
        }

        @DisplayName("목표값이 현재값보다 작으면 업적이 달성된다")
        @Test
        fun `목표값이 현재값보다 작으면 업적이 달성된다`() {
            //given
            val currentValue = 1
            val achieveRequest = AchievementAchieveRequest(AchievementType.QUEST_REGISTRATION, currentValue, 1L)

            val targetValue = currentValue - 1
            val mustAchieve = Achievement(AchievementType.QUEST_REGISTRATION, targetValue)
            doReturn(mustAchieve).`when`(achievementQueryService).getNotAchievedAchievement(any(), any())

            //when
            achievementService.checkAndAchieveAchievements(achieveRequest)

            //then
            assertThat(savedAchievements).isNotEmpty.contains(mustAchieve)
        }

        @DisplayName("목표값이 현재값과 같으면 업적이 달성된다")
        @Test
        fun `목표값이 현재값과 같으면 업적이 달성된다`() {
            //given
            val currentValue = 1
            val achieveRequest = AchievementAchieveRequest(AchievementType.QUEST_REGISTRATION, currentValue, 1L)

            val targetValue = currentValue
            val mustAchieve = Achievement(AchievementType.QUEST_REGISTRATION, targetValue)
            doReturn(mustAchieve).`when`(achievementQueryService).getNotAchievedAchievement(any(), any())

            //when
            achievementService.checkAndAchieveAchievements(achieveRequest)

            //then
            assertThat(savedAchievements).isNotEmpty.contains(mustAchieve)
        }

        @DisplayName("목표값이 현재값보다 크면 업적이 달성되지 않는다")
        @Test
        fun `목표값이 현재값보다 크면 업적이 달성되지 않는다`() {
            //given
            val currentValue = 1
            val achieveRequest = AchievementAchieveRequest(AchievementType.QUEST_REGISTRATION, currentValue, 1L)

            val targetValue = currentValue + 1
            val mustNotAchieve = Achievement(AchievementType.QUEST_REGISTRATION, targetValue)
            doReturn(mustNotAchieve).`when`(achievementQueryService).getNotAchievedAchievement(any(), any())

            //when
            achievementService.checkAndAchieveAchievements(achieveRequest)

            //then
            assertThat(savedAchievements).doesNotContain(mustNotAchieve)
        }
    }
}