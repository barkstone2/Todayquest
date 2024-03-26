package dailyquest.batch.job

import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.repository.JobRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class PerfectDayBatchConfig {
    @Bean
    fun perfectDayBatchJob(
        jobRepository: JobRepository,
        perfectDayLogStep: Step,
        perfectDayAchievementStep: Step
        perfectDayAchievementStep: Step,
        achieveNotificationStep: Step,
    ): Job {
        return JobBuilder("perfectDayJob", jobRepository)
            .start(perfectDayLogStep)
            .next(perfectDayAchievementStep)
            .next(achieveNotificationStep)
            .build()
    }
}