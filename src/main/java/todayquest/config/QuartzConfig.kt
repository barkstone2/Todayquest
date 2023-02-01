package todayquest.config

import org.quartz.*
import org.quartz.impl.StdSchedulerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class QuartzConfig {

    @Bean
    fun jobDetail(questFailQuartzJob: Job): JobDetail {
        return JobBuilder
            .newJob(questFailQuartzJob.javaClass)
            .withIdentity("springBatchJob")
            .storeDurably()
            .build()
    }

    @Bean
    fun trigger(jobDetail: JobDetail): Trigger {
        return TriggerBuilder
            .newTrigger()
            .forJob(jobDetail)
            .withIdentity("batchJobTrigger")
            .withSchedule(CronScheduleBuilder.cronSchedule("0 0 * * * ?"))
            .build()
    }

    @Bean
    fun scheduler(trigger: Trigger, jobDetail: JobDetail): Scheduler {
        val schedulerFactory: SchedulerFactory = StdSchedulerFactory()
        val scheduler = schedulerFactory.scheduler
        scheduler.start()
        scheduler.scheduleJob(jobDetail, trigger)
        return scheduler
    }
}