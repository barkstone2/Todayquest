package dailyquest.sqs.listener

import dailyquest.properties.BatchParameterProperties
import io.awspring.cloud.sqs.annotation.SqsListener
import io.awspring.cloud.sqs.listener.acknowledgement.Acknowledgement
import org.springframework.batch.core.ExitStatus
import org.springframework.batch.core.Job
import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.stereotype.Component

@Component
class AchievementRegQueueListener(
    private val jobLauncher: JobLauncher,
    private val checkAndAchieveBatchJob: Job,
    private val batchParameterProperties: BatchParameterProperties,
) {
    @SqsListener(value = ["\${aws.sqs.queue.batch-job-queue-url}"], acknowledgementMode = "MANUAL")
    fun consumeMessage(achievementId: Long, ack: Acknowledgement) {
        val jobExecution = this.runCheckAndAchieveJob(achievementId)
        if (jobExecution.exitStatus.equals(ExitStatus.COMPLETED)) {
            ack.acknowledge()
        }
    }

    private fun runCheckAndAchieveJob(achievementId: Long): JobExecution {
        val jobParameters = JobParametersBuilder()
            .addLong(batchParameterProperties.targetAchievementIdKey, achievementId)
            .toJobParameters()
        return jobLauncher.run(checkAndAchieveBatchJob, jobParameters)
    }
}