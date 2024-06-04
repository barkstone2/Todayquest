package dailyquest.batch.step

import dailyquest.achievement.entity.Achievement
import dailyquest.achievement.entity.AchievementAchieveLog
import dailyquest.achievement.repository.AchievementAchieveLogRepository
import dailyquest.batch.listener.step.CheckAndAchieveStepListener
import dailyquest.user.record.repository.BatchUserRecordRepository
import dailyquest.user.repository.BatchUserRepository
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.JobScope
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.item.ItemProcessor
import org.springframework.batch.item.ItemReader
import org.springframework.batch.item.ItemWriter
import org.springframework.batch.item.data.RepositoryItemReader
import org.springframework.batch.item.data.RepositoryItemWriter
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder
import org.springframework.batch.item.function.FunctionItemProcessor
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager

@Configuration(proxyBeanMethods = false)
class CheckAndAchieveStepConfig {
    @Bean
    @JobScope
    fun checkAndAchieveStep(
        jobRepository: JobRepository,
        transactionManager: PlatformTransactionManager,
        checkAndAchieveReader: ItemReader<Long>,
        checkAndAchieveProcessor: ItemProcessor<Long, AchievementAchieveLog>,
        checkAndAchieveWriter: ItemWriter<AchievementAchieveLog>,
        checkAndAchieveStepListener: CheckAndAchieveStepListener
    ): Step {
        return StepBuilder("checkAndAchieveStep", jobRepository)
            .chunk<Long, AchievementAchieveLog>(10, transactionManager)
            .reader(checkAndAchieveReader)
            .processor(checkAndAchieveProcessor)
            .writer(checkAndAchieveWriter)
            .listener(checkAndAchieveStepListener)
            .faultTolerant()
            .retryLimit(3)
            .retry(Exception::class.java)
            .build()
    }

    @Bean
    @StepScope
    fun checkAndAchieveReader(
        @Value("#{jobExecutionContext['targetAchievement']}") targetAchievement: Achievement,
        batchUserRecordRepository: BatchUserRecordRepository,
    ): RepositoryItemReader<Long> {
        return RepositoryItemReaderBuilder<Long>()
            .repository(batchUserRecordRepository)
            .methodName("getAllUserIdWhoCanAchieveOf")
            .arguments(targetAchievement)
            .pageSize(10)
            .name("checkAndAchieveReader")
            .sorts(sortedMapOf())
            .build()
    }

    @Bean
    @StepScope
    fun checkAndAchieveProcessor(
        @Value("#{jobExecutionContext['targetAchievement']}") targetAchievement: Achievement,
    ): FunctionItemProcessor<Long, AchievementAchieveLog> {
        return FunctionItemProcessor {
            AchievementAchieveLog.of(targetAchievement, it)
        }
    }

    @Bean
    @StepScope
    fun checkAndAchieveWriter(
        achievementAchieveLogRepository: AchievementAchieveLogRepository
    ): RepositoryItemWriter<AchievementAchieveLog> {
        return RepositoryItemWriterBuilder<AchievementAchieveLog>()
            .repository(achievementAchieveLogRepository)
            .build()
    }
}