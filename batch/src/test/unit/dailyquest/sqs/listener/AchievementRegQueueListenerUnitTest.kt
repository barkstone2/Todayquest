package dailyquest.sqs.listener

import dailyquest.properties.BatchParameterProperties
import io.awspring.cloud.sqs.listener.acknowledgement.Acknowledgement
import io.mockk.Called
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.batch.core.ExitStatus
import org.springframework.batch.core.Job
import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.core.launch.JobLauncher

@ExtendWith(MockKExtension::class)
@DisplayName("업적 등록 큐 리스너 유닛 테스트")
class AchievementRegQueueListenerUnitTest {

    @RelaxedMockK
    lateinit var jobLauncher: JobLauncher
    @RelaxedMockK
    lateinit var checkAndAchieveBatchJob: Job
    @RelaxedMockK
    lateinit var batchParameterProperties: BatchParameterProperties
    @InjectMockKs
    lateinit var achievementRegQueueListener: AchievementRegQueueListener
    @RelaxedMockK
    lateinit var jobExecution: JobExecution
    @RelaxedMockK
    lateinit var acknowledgement: Acknowledgement

    @DisplayName("리스너 호출 시")
    @Nested
    inner class WhenListenerCalled {
        private val achievementId = 1L

        @DisplayName("업적 ID를 잡 파라미터로 배치 작업을 실행한다")
        @Test
        fun `업적 ID를 잡 파라미터로 배치 작업을 실행한다`() {
            //given
            val jobParameters = JobParametersBuilder()
                .addLong(batchParameterProperties.targetAchievementIdKey, achievementId)
                .toJobParameters()

            //when
            achievementRegQueueListener.consumeMessage(achievementId, acknowledgement)

            //then
            verify {
                jobLauncher.run(eq(checkAndAchieveBatchJob), eq(jobParameters))
            }
        }

        @DisplayName("배치 작업의 종료 상태가 COMPLETE로 끝나면 acknowledge가 호출된다")
        @Test
        fun `배치 작업의 종료 상태가 COMPLETE로 끝나면 acknowledge가 호출된다`() {
            //given
            every { jobLauncher.run(any(), any()) } returns jobExecution
            every { jobExecution.exitStatus } returns ExitStatus.COMPLETED

            //when
            achievementRegQueueListener.consumeMessage(achievementId, acknowledgement)

            //then
            verify { acknowledgement.acknowledge() }
        }

        @DisplayName("배치 작업의 종료 상태가 COMPLETE로 끝나지 않으면 acknowledge가 호출되지 않는다")
        @Test
        fun `배치 작업의 종료 상태가 COMPLETE로 끝나지 않으면 acknowledge가 호출되지 않는다`() {
            //given
            every { jobLauncher.run(any(), any()) } returns jobExecution
            every { jobExecution.exitStatus } returns ExitStatus.FAILED

            //when
            achievementRegQueueListener.consumeMessage(achievementId, acknowledgement)

            //then
            verify { acknowledgement wasNot Called }
        }
    }
}