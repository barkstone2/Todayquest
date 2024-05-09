package dailyquest.batch.listener.job

import dailyquest.achievement.entity.Achievement
import dailyquest.achievement.repository.AchievementRepository
import dailyquest.common.util.JobExecutionContextUtil
import dailyquest.properties.BatchContextProperties
import dailyquest.properties.BatchParameterProperties
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.JobParameters
import org.springframework.data.repository.findByIdOrNull

@ExtendWith(MockKExtension::class)
class CheckAndAchieveJobListenerUnitTest {
    @InjectMockKs
    private lateinit var listener: CheckAndAchieveJobListener
    @RelaxedMockK
    private lateinit var achievementRepository: AchievementRepository
    @RelaxedMockK
    private lateinit var batchParameterProperties: BatchParameterProperties
    @RelaxedMockK
    private lateinit var batchContextProperties: BatchContextProperties
    @RelaxedMockK
    private lateinit var jobParameters: JobParameters
    private lateinit var jobExecution: JobExecution
    private lateinit var executionContextUtil: JobExecutionContextUtil

    @BeforeEach
    fun init() {
        jobExecution = JobExecution(1, jobParameters)
        executionContextUtil = spyk(JobExecutionContextUtil.from(jobExecution))
        mockkObject(JobExecutionContextUtil)
        every { JobExecutionContextUtil.from(eq(jobExecution)) } returns executionContextUtil
        every { achievementRepository.findByIdOrNull(any()) } returns mockk(relaxed = true)
    }

    @DisplayName("beforeJob 요청시")
    @Nested
    inner class TestBeforeJob {
        @DisplayName("jobExecution으로 JobExecutionContextUtil을 초기화한다")
        @Test
        fun `jobExecution으로 JobExecutionContextUtil을 초기화한다`() {
            //given
            //when
            listener.beforeJob(jobExecution)

            //then
            verify { JobExecutionContextUtil.from(eq(jobExecution)) }
        }

        @DisplayName("JobParameter에 담긴 업적 ID로 업적을 조회한다")
        @Test
        fun `JobParameter에 담긴 업적 ID로 업적을 조회한다`() {
            //given
            val achievementId = 1L
            every { jobParameters.getLong(any()) } returns achievementId

            //when
            listener.beforeJob(jobExecution)

            //then
            verify { achievementRepository.findById(eq(achievementId)) }
        }

        @DisplayName("조회한 업적을 JobExecutionContex에 담는다")
        @Test
        fun `조회한 업적을 JobExecutionContext에 담는다`() {
            //given
            val targetAchievement = mockk<Achievement>()
            every { achievementRepository.findByIdOrNull(any()) } returns targetAchievement

            //when
            listener.beforeJob(jobExecution)

            //then
            verify { executionContextUtil.putToJobContext(any(), eq(targetAchievement)) }
        }
    }
}