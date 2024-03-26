package dailyquest.batch.step

import dailyquest.achievement.entity.AchievementAchieveLog
import dailyquest.batch.listener.step.AchievementNotificationStepListener
import dailyquest.notification.dto.AchieveNotificationSaveRequest
import dailyquest.notification.entity.Notification
import dailyquest.notification.repository.NotificationRepository
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.JobScope
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.item.ItemReader
import org.springframework.batch.item.ItemWriter
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder
import org.springframework.batch.item.function.FunctionItemProcessor
import org.springframework.batch.item.support.IteratorItemReader
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager

@Configuration(proxyBeanMethods = false)
class AchievementNotificationStepConfig {
    @Bean
    @JobScope
    fun achieveNotificationStep(
        jobRepository: JobRepository,
        transactionManager: PlatformTransactionManager,
        achievedLogsReader: ItemReader<AchievementAchieveLog>,
        achievementNotificationProcessor: FunctionItemProcessor<AchievementAchieveLog, Notification>,
        achievementNotificationWriter: ItemWriter<Notification>,
        achievementNotificationStepListener: AchievementNotificationStepListener
    ): Step {
        return StepBuilder("achieveNotificationStep", jobRepository)
            .chunk<AchievementAchieveLog, Notification>(10, transactionManager)
            .reader(achievedLogsReader)
            .processor(achievementNotificationProcessor)
            .writer(achievementNotificationWriter)
            .listener(achievementNotificationStepListener)
            .faultTolerant()
            .retryLimit(3)
            .retry(Exception::class.java)
            .build()
    }

    @Bean
    @StepScope
    fun achievedLogsReader(
        @Value("#{jobExecutionContext['achievedLogs']}") achievedLogs: List<AchievementAchieveLog>,
    ): ItemReader<AchievementAchieveLog> {
        return IteratorItemReader(achievedLogs)
    }

    @Bean
    @StepScope
    fun achievementNotificationProcessor(): FunctionItemProcessor<AchievementAchieveLog, Notification> {
        return FunctionItemProcessor {
            AchieveNotificationSaveRequest.of(it.userId, it.achievement).mapToEntity()
        }
    }

    @Bean
    @StepScope
    fun achievementNotificationWriter(
        notificationRepository: NotificationRepository
    ): ItemWriter<Notification> {
        return RepositoryItemWriterBuilder<Notification>()
            .repository(notificationRepository)
            .build()
    }
}