package dailyquest.batch.service

import dailyquest.properties.BatchParameterProperties
import org.springframework.batch.core.Job
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service

@Async
@Service
class JobService(
    private val jobLauncher: JobLauncher,
    private val checkAndAchieveBatchJob: Job,
    private val batchParameterProperties: BatchParameterProperties,
) {
    fun runCheckAndAchieveJob(achievementId: Long) {
        val jobParameters = JobParametersBuilder()
            .addLong(batchParameterProperties.targetAchievementIdKey, achievementId)
            .toJobParameters()
        jobLauncher.run(checkAndAchieveBatchJob, jobParameters)
    }
}