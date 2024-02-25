package dailyquest.achivement.service

import dailyquest.achievement.entity.Achievement
import dailyquest.achievement.entity.AchievementType
import dailyquest.achievement.service.AchievementLogService
import dailyquest.achievement.service.AchievementQueryService
import dailyquest.achievement.service.AchievementService
import dailyquest.log.gold.earn.service.GoldEarnLogService
import dailyquest.quest.service.QuestLogService
import dailyquest.user.dto.UserPrincipal
import dailyquest.user.service.UserService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*

@ExtendWith(MockitoExtension::class)
@DisplayName("업적 서비스 단위 테스트")
class AchievementServiceUnitTest {

    @Mock
    lateinit var achievementQueryService: AchievementQueryService
    @Mock
    lateinit var questLogService: QuestLogService
    @Mock
    lateinit var achievementLogService: AchievementLogService
    @Mock
    lateinit var userService: UserService
    @Mock
    lateinit var goldEarnLogService: GoldEarnLogService

    @InjectMocks
    lateinit var achievementService: AchievementService

    @DisplayName("업적 확인 후 달성 요청 시")
    @Nested
    inner class TestCheckAndAchieveAchievements {

        private val savedAchievements = mutableListOf<Achievement>()

        @BeforeEach
        fun init() {
            savedAchievements.clear()
            doAnswer { savedAchievements.addAll(it.getArgument(0)) }.`when`(achievementLogService).achieveAll(any(), any())
        }

        @DisplayName("목표값이 현재값보다 작은 업적이 달성된다")
        @Test
        fun `목표값이 현재값보다 작은 업적이 달성된다`() {
            //given
            val targetValue = 0
            val currentValue = 1
            val mustAchieve = Achievement(AchievementType.QUEST_TOTAL_REGISTRATION, targetValue)
            doReturn(currentValue).`when`(questLogService).getTotalRegistrationCount(any())
            doReturn(listOf(mustAchieve)).`when`(achievementQueryService).getNotAchievedAchievements(any(), any())

            //when
            achievementService.checkAndAchieveAchievements(AchievementType.QUEST_TOTAL_REGISTRATION, 1L)

            //then
            assertThat(savedAchievements).isNotEmpty.contains(mustAchieve)
        }

        @DisplayName("목표값이 현재값과 같은 업적이 달성된다")
        @Test
        fun `목표값이 현재값과 같은 업적이 달성된다`() {
            //given
            val targetValue = 1
            val currentValue = 1
            val mustAchieve = Achievement(AchievementType.QUEST_TOTAL_REGISTRATION, targetValue)
            doReturn(currentValue).`when`(questLogService).getTotalRegistrationCount(any())
            doReturn(listOf(mustAchieve)).`when`(achievementQueryService).getNotAchievedAchievements(any(), any())

            //when
            achievementService.checkAndAchieveAchievements(AchievementType.QUEST_TOTAL_REGISTRATION, 1L)

            //then
            assertThat(savedAchievements).isNotEmpty.contains(mustAchieve)
        }

        @DisplayName("목표값이 현재값보다 큰 업적은 달성되지 않는다")
        @Test
        fun `목표값이 현재값보다 큰 업적은 달성되지 않는다`() {
            //given
            val targetValue = 2
            val currentValue = 1
            val mustNotAchieve = Achievement(AchievementType.QUEST_TOTAL_REGISTRATION, targetValue)
            doReturn(currentValue).`when`(questLogService).getTotalRegistrationCount(any())
            doReturn(listOf(mustNotAchieve)).`when`(achievementQueryService).getNotAchievedAchievements(any(), any())

            //when
            achievementService.checkAndAchieveAchievements(AchievementType.QUEST_TOTAL_REGISTRATION, 1L)

            //then
            assertThat(savedAchievements).doesNotContain(mustNotAchieve)
        }

        @DisplayName("QUEST_TOTAL_REGISTRATION 타입에 대한 요청인 경우 questLogService로부터 현재값을 조회한다")
        @Test
        fun `QUEST_TOTAL_REGISTRATION 타입에 대한 요청인 경우 questLogService로부터 현재값을 조회한다`() {
            //given
            val achievementType = AchievementType.QUEST_TOTAL_REGISTRATION
            val userId = 1L
            doReturn(listOf(Achievement(achievementType, 0))).`when`(achievementQueryService).getNotAchievedAchievements(any(), any())

            //when
            achievementService.checkAndAchieveAchievements(achievementType, userId)

            //then
            verify(questLogService, atLeastOnce()).getTotalRegistrationCount(eq(userId))
        }

        @DisplayName("QUEST_TOTAL_COMPLETION 타입에 대한 요청인 경우 questLogService로부터 현재값을 조회한다")
        @Test
        fun `QUEST_TOTAL_COMPLETION 타입에 대한 요청인 경우 questLogService로부터 현재값을 조회한다`() {
            //given
            val achievementType = AchievementType.QUEST_TOTAL_COMPLETION
            val userId = 1L
            doReturn(listOf(Achievement(achievementType, 0))).`when`(achievementQueryService).getNotAchievedAchievements(any(), any())

            //when
            achievementService.checkAndAchieveAchievements(achievementType, userId)

            //then
            verify(questLogService, atLeastOnce()).getTotalCompletionCount(eq(userId))
        }

        @DisplayName("QUEST_CONTINUOUS_REGISTRATION_DAYS 타입에 대한 요청인 경우 questLogService로부터 현재값을 조회한다")
        @Test
        fun `QUEST_CONTINUOUS_REGISTRATION_DAYS 타입에 대한 요청인 경우 questLogService로부터 현재값을 조회한다`() {
            //given
            val achievementType = AchievementType.QUEST_CONTINUOUS_REGISTRATION_DAYS
            val userId = 1L
            val targetValue = 0
            doReturn(listOf(Achievement(achievementType, targetValue))).`when`(achievementQueryService).getNotAchievedAchievements(any(), any())

            //when
            achievementService.checkAndAchieveAchievements(achievementType, userId)

            //then
            verify(questLogService, atLeastOnce()).getContinuousRegistrationCount(eq(userId), eq(targetValue))
        }

        @DisplayName("USER_LEVEL 타입에 대한 요청인 경우 userService로부터 현재값을 조회한다")
        @Test
        fun `USER_LEVEL 타입에 대한 요청인 경우 userService로부터 현재값을 조회한다`() {
            //given
            val achievementType = AchievementType.USER_LEVEL
            val userId = 1L
            doReturn(listOf(Achievement(achievementType, 0))).`when`(achievementQueryService).getNotAchievedAchievements(any(), any())
            val user = mock<UserPrincipal>()
            doReturn(user).`when`(userService).getUserById(any())

            //when
            achievementService.checkAndAchieveAchievements(achievementType, userId)

            //then
            verify(userService, atLeastOnce()).getUserById(eq(userId))
            verify(user, atLeastOnce()).level
        }

        @DisplayName("USER_GOLD_EARN 타입에 대한 요청인 경우 goldEarnLogService로부터 현재값을 조회한다")
        @Test
        fun `USER_GOLD_EARN 타입에 대한 요청인 경우 goldEarnLogService로부터 현재값을 조회한다`() {
            //given
            val achievementType = AchievementType.USER_GOLD_EARN
            val userId = 1L
            doReturn(listOf(Achievement(achievementType, 0))).`when`(achievementQueryService).getNotAchievedAchievements(any(), any())

            //when
            achievementService.checkAndAchieveAchievements(achievementType, userId)

            //then
            verify(goldEarnLogService, atLeastOnce()).getTotalGoldEarnOfUser(eq(userId))
        }
    }
}