package dailyquest.batch.listener.step

import dailyquest.achievement.entity.Achievement
import dailyquest.achievement.entity.AchievementType
import dailyquest.achievement.repository.AchievementRepository
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.StepExecution
import org.springframework.batch.item.ExecutionContext

@ExtendWith(MockKExtension::class)
@DisplayName("완벽한 하루 업적 스텝 리스너 유닛 테스트")
class PerfectDayAchievementStepListenerUnitTest {
    @RelaxedMockK
    private lateinit var stepExecution: StepExecution
    @RelaxedMockK
    private lateinit var jobExecution: JobExecution
    @RelaxedMockK
    private lateinit var stepExecutionContext: ExecutionContext
    @RelaxedMockK
    private lateinit var jobExecutionContext: ExecutionContext
    @RelaxedMockK
    private lateinit var achievementRepository: AchievementRepository
    private lateinit var perfectDayAchievementStepListener: PerfectDayAchievementStepListener
    private val perfectDayAchievementsKey = "perfectDayAchievements"
    private val achievedUserIdsKey = "achievedUserIds"

    @BeforeEach
    fun init() {
        perfectDayAchievementStepListener = PerfectDayAchievementStepListener(achievementRepository)
        perfectDayAchievementStepListener.beforeStep(stepExecution)

        every { stepExecution.executionContext } returns stepExecutionContext
        every { stepExecution.jobExecution } returns jobExecution
        every { jobExecution.executionContext } returns jobExecutionContext
    }

    @DisplayName("beforeStep 호출 시 PERFECT_DAY 타입의 업적을 조회해 StepExecutionContext에 담는다")
    @Test
    fun `beforeStep 호출 시 PERFECT_DAY 타입의 업적을 조회해 StepExecutionContext에 담는다`() {
        //given
        val achievements = listOf<Achievement>()
        every { achievementRepository.getAllByType(eq(AchievementType.PERFECT_DAY)) } returns achievements

        //when
        perfectDayAchievementStepListener.beforeStep(stepExecution)

        //then
        verify { stepExecutionContext.put(eq(perfectDayAchievementsKey), eq(achievements)) }
    }


    @DisplayName("afterWrite 호출 시")
    @Nested
    inner class TestAfterWrite {

        @DisplayName("jobExecutionContext로 부터 기존 유저 아이디 목록을 조회한다")
        @Test
        fun `jobExecutionContext로 부터 기존 유저 아이디 목록을 조회한다`() {
            //given
            every { jobExecutionContext.get(eq(achievedUserIdsKey)) } returns mutableListOf<Long>()

            //when
            perfectDayAchievementStepListener.afterWrite(mockk(relaxed = true))

            //then
            verify { jobExecutionContext.get(eq(achievedUserIdsKey)) }
        }

        @DisplayName("jobExecutionContext에 기존 목록이 없으면 빈 목록을 반환한다")
        @Test
        fun `jobExecutionContext에 기존 목록이 없으면 빈 목록을 반환한다`() {
            //given
            every { jobExecutionContext.get(eq(achievedUserIdsKey)) } returns null

            //when
            perfectDayAchievementStepListener.afterWrite(mockk(relaxed = true))

            //then
            verify { jobExecutionContext.get(eq(achievedUserIdsKey)) }
        }

        @DisplayName("jobExecutionContext에 유저 아이디 목록이 저장된다")
        @Test
        fun `jobExecutionContext에 유저 아이디 목록이 저장된다`() {
            //given
            val userIds = mutableListOf<Long>()
            every { jobExecutionContext.get(eq(achievedUserIdsKey)) } returns userIds

            //when
            perfectDayAchievementStepListener.afterWrite(mockk(relaxed = true))

            //then
            verify { jobExecutionContext.put(eq(achievedUserIdsKey), eq(userIds)) }
        }
    }


}