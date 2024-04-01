package dailyquest.batch.step

import dailyquest.achievement.entity.AchievementAchieveLog
import dailyquest.batch.listener.step.AchievementAchieveNotificationStepListener
import dailyquest.notification.dto.AchieveNotificationSaveRequest
import dailyquest.notification.entity.Notification
import dailyquest.notification.repository.NotificationRepository
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.JobScope
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.item.ItemProcessor
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
class AchievementAchieveNotificationStepConfig {
    @Bean
    @JobScope
    fun achievementAchieveNotificationStep(
        jobRepository: JobRepository,
        transactionManager: PlatformTransactionManager,
        achievementAchieveLogReader: ItemReader<AchievementAchieveLog>,
        achievementAchieveNotificationProcessor: ItemProcessor<AchievementAchieveLog, Notification>,
        achievementAchieveNotificationWriter: ItemWriter<Notification>,
        achievementNotificationStepListener: AchievementAchieveNotificationStepListener
    ): Step {
        return StepBuilder("achievementAchieveNotificationStep", jobRepository)
            .chunk<AchievementAchieveLog, Notification>(10, transactionManager)
            .reader(achievementAchieveLogReader)
            .processor(achievementAchieveNotificationProcessor)
            .writer(achievementAchieveNotificationWriter)
            .listener(achievementNotificationStepListener)
            .faultTolerant()
            .retryLimit(3)
            .retry(Exception::class.java)
            .build()
    }

    @Bean
    @StepScope
    fun achievementAchieveLogReader(
        @Value("#{jobExecutionContext['achievedLogs']}") achievedLogs: List<AchievementAchieveLog>,
    ): ItemReader<AchievementAchieveLog> {
        return IteratorItemReader(achievedLogs)
    }

    @Bean
    @StepScope
    fun achievementAchieveNotificationProcessor(): FunctionItemProcessor<AchievementAchieveLog, Notification> {
        return FunctionItemProcessor {
            AchieveNotificationSaveRequest.of(it.userId, it.achievement).mapToEntity()
        }
    }

    @Bean
    @StepScope
    fun achievementAchieveNotificationWriter(
        notificationRepository: NotificationRepository
    ): ItemWriter<Notification> {
        return RepositoryItemWriterBuilder<Notification>()
            .repository(notificationRepository)
            .build()
    }
}