package todayquest.config

import org.quartz.*
import org.quartz.impl.StdSchedulerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class QuartzConfig {

    @Bean
    fun questResetJobDetail(questResetQuartzJob: Job): JobDetail {
        return JobBuilder
            .newJob(questResetQuartzJob.javaClass)
            .withIdentity("questResetJob")
            .storeDurably()
            .build()
    }

    @Bean
    fun questDeadLineJobDetail(questDeadLineQuartzJob: Job): JobDetail {
        return JobBuilder
            .newJob(questDeadLineQuartzJob.javaClass)
            .withIdentity("questDeadLineJob")
            .storeDurably()
            .build()
    }

    @Bean
    fun questResetTrigger(questResetJobDetail: JobDetail): Trigger {
        return TriggerBuilder
            .newTrigger()
            .forJob(questResetJobDetail)
            .withIdentity("questResetTrigger")
            .withSchedule(CronScheduleBuilder.cronSchedule("0 0 * * * ?"))
            .build()
    }

    @Bean
    fun questDeadLineTrigger(questDeadLineJobDetail: JobDetail): Trigger {
        return TriggerBuilder
            .newTrigger()
            .forJob(questDeadLineJobDetail)
            .withIdentity("questDeadLineTrigger")
            .withSchedule(CronScheduleBuilder.cronSchedule("0 * * * * ?"))
            .build()
    }

    @Bean
    fun scheduler(
        questResetJobDetail: JobDetail,
        questResetTrigger: Trigger,
        questDeadLineJobDetail: JobDetail,
        questDeadLineTrigger: Trigger,
    ): Scheduler {
        val schedulerFactory: SchedulerFactory = StdSchedulerFactory()
        val scheduler = schedulerFactory.scheduler
        scheduler.start()
        scheduler.scheduleJob(questResetJobDetail, questResetTrigger)
        scheduler.scheduleJob(questDeadLineJobDetail, questDeadLineTrigger)
        return scheduler
    }


}