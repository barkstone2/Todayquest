package dailyquest.batch.job

import dailyquest.batch.listener.job.CheckAndAchieveJobListener
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.repository.JobRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class CheckAndAchieveBatchConfig {
    @Bean
    fun checkAndAchieveBatchJob(
        jobRepository: JobRepository,
        checkAndAchieveStep: Step,
        achievementAchieveNotificationStep: Step,
        checkAndAchieveJobListener: CheckAndAchieveJobListener
    ): Job {
        return JobBuilder("checkAndAchieveJob", jobRepository)
            .start(checkAndAchieveStep)
            .next(achievementAchieveNotificationStep)
            .listener(checkAndAchieveJobListener)
            .build()
    }
}