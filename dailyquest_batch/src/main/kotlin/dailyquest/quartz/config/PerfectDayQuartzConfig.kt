package dailyquest.quartz.config

import org.quartz.*
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class PerfectDayQuartzConfig {
    @Bean
    fun perfectDayJobDetail(perfectDayQuartzJob: Job): JobDetail {
        return JobBuilder
            .newJob(perfectDayQuartzJob.javaClass)
            .withIdentity("perfectDayJobDetail")
            .storeDurably()
            .build()
    }

    @Bean
    fun perfectDayJobTrigger(perfectDayJobDetail: JobDetail): Trigger {
        return TriggerBuilder
            .newTrigger()
            .forJob(perfectDayJobDetail)
            .withIdentity("perfectDayJobTrigger")
            .withSchedule(CronScheduleBuilder.cronSchedule("0 0 6 * * ?"))
            .build()
    }
}