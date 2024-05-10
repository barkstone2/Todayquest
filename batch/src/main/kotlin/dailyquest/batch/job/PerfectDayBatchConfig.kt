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
        readPerfectDayUserIdStep: Step,
        increasePerfectDayCountStep: Step,
        perfectDayAchievementStep: Step,
        achievementAchieveNotificationStep: Step,
    ): Job {
        return JobBuilder("perfectDayJob", jobRepository)
            .start(readPerfectDayUserIdStep)
            .next(increasePerfectDayCountStep)
            .next(perfectDayAchievementStep)
            .next(achievementAchieveNotificationStep)
            .build()
    }
}