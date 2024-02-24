package dailyquest.achivement.service

import dailyquest.achievement.entity.Achievement
import dailyquest.achievement.entity.AchievementType
import dailyquest.achievement.service.AchievementLogService
import dailyquest.achievement.service.AchievementQueryService
import dailyquest.achievement.service.AchievementService
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

        @DisplayName("QUEST_TOTAL_REGISTRATION 타입에 대한 요청인 경우 목표값이 반환값보다 작거나 같은 업적이 달성된다")
        @Test
        fun `QUEST_TOTAL_REGISTRATION 타입에 대한 요청인 경우 목표값이 반환값보다 작거나 같은 업적이 달성된다`() {
            //given
            val achievementType = AchievementType.QUEST_TOTAL_REGISTRATION
            val userId = 1L
            val notAchievedAchievements = listOf(
                Achievement(achievementType, 0),
                Achievement(achievementType, 1),
            )
            doReturn(notAchievedAchievements).`when`(achievementQueryService).getNotAchievedAchievements(any(), any())
            doReturn(1).`when`(questLogService).getTotalRegistrationCount(any())

            //when
            achievementService.checkAndAchieveAchievements(achievementType, userId)

            //then
            assertThat(savedAchievements).isNotEmpty.containsAll(notAchievedAchievements)
        }

        @DisplayName("QUEST_TOTAL_COMPLETION 타입에 대한 요청인 경우 목표값이 반환값보다 작거나 같은 업적이 달성된다")
        @Test
        fun `QUEST_TOTAL_COMPLETION 타입에 대한 요청인 경우 목표값이 반환값보다 작거나 같은 업적이 달성된다`() {
            //given
            val achievementType = AchievementType.QUEST_TOTAL_COMPLETION
            val userId = 1L
            val notAchievedAchievements = listOf(
                Achievement(achievementType, 0),
                Achievement(achievementType, 1),
            )
            doReturn(notAchievedAchievements).`when`(achievementQueryService).getNotAchievedAchievements(any(), any())
            doReturn(1).`when`(questLogService).getTotalCompletionCount(any())

            //when
            achievementService.checkAndAchieveAchievements(achievementType, userId)

            //then
            assertThat(savedAchievements).isNotEmpty.containsAll(notAchievedAchievements)
        }

        @DisplayName("QUEST_CONTINUOUS_REGISTRATION_DAYS 타입에 대한 요청인 경우 목표값이 반환값보다 작거나 같은 업적이 달성된다")
        @Test
        fun `QUEST_CONTINUOUS_REGISTRATION_DAYS 타입에 대한 요청인 경우 목표값이 반환값보다 작거나 같은 업적이 달성된다`() {
            //given
            val achievementType = AchievementType.QUEST_CONTINUOUS_REGISTRATION_DAYS
            val userId = 1L
            val notAchievedAchievements = listOf(
                Achievement(achievementType, 0),
                Achievement(achievementType, 1),
            )
            doReturn(notAchievedAchievements).`when`(achievementQueryService).getNotAchievedAchievements(any(), any())
            doReturn(1).`when`(questLogService).getContinuousRegistrationCount(any(), any())

            //when
            achievementService.checkAndAchieveAchievements(achievementType, userId)

            //then
            assertThat(savedAchievements).isNotEmpty.containsAll(notAchievedAchievements)
        }

        @DisplayName("USER_LEVEL 타입에 대한 요청인 경우 목표값이 반환값보다 작거나 같은 업적이 달성된다")
        @Test
        fun `USER_LEVEL 타입에 대한 요청인 경우 목표값이 반환값보다 작거나 같은 업적이 달성된다`() {
            //given
            val achievementType = AchievementType.USER_LEVEL
            val userId = 1L
            val notAchievedAchievements = listOf(
                Achievement(achievementType, 0),
                Achievement(achievementType, 1),
            )
            doReturn(notAchievedAchievements).`when`(achievementQueryService).getNotAchievedAchievements(any(), any())
            val user = mock<UserPrincipal>()
            doReturn(user).`when`(userService).getUserById(any())
            doReturn(1).`when`(user).level

            //when
            achievementService.checkAndAchieveAchievements(achievementType, userId)

            //then
            assertThat(savedAchievements).isNotEmpty.containsAll(notAchievedAchievements)
        }
    }
}