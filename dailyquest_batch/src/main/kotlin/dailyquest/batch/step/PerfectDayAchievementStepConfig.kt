package dailyquest.batch.step

import dailyquest.achievement.entity.Achievement
import dailyquest.achievement.entity.AchievementAchieveLog
import dailyquest.achievement.repository.AchievementAchieveLogRepository
import dailyquest.batch.listener.step.PerfectDayAchievementStepListener
import dailyquest.perfectday.dto.PerfectDayCount
import dailyquest.perfectday.repository.PerfectDayLogRepository
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.JobScope
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.item.ItemProcessor
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
class PerfectDayAchievementStepConfig {
    @Bean
    @JobScope
    fun perfectDayAchievementStep(
        jobRepository: JobRepository,
        transactionManager: PlatformTransactionManager,
        perfectDayCountReader: RepositoryItemReader<PerfectDayCount>,
        perfectDayAchievementProcessor: ItemProcessor<PerfectDayCount, AchievementAchieveLog>,
        perfectDayAchievementWriter: ItemWriter<AchievementAchieveLog>,
        perfectDayAchievementStepListener: PerfectDayAchievementStepListener
    ): Step {
        return StepBuilder("perfectDayAchievementStep", jobRepository)
            .chunk<PerfectDayCount, AchievementAchieveLog>(10, transactionManager)
            .reader(perfectDayCountReader)
            .processor(perfectDayAchievementProcessor)
            .writer(perfectDayAchievementWriter)
            .listener(perfectDayAchievementStepListener)
            .faultTolerant()
            .retryLimit(3)
            .retry(Exception::class.java)
            .build()
    }

    @Bean
    @StepScope
    fun perfectDayCountReader(
        perfectDayLogRepository: PerfectDayLogRepository,
        @Value("#{jobExecutionContext['perfectDayLogUserIds']}") userIds: List<Long>
    ): RepositoryItemReader<PerfectDayCount> {
        return RepositoryItemReaderBuilder<PerfectDayCount>()
            .repository(perfectDayLogRepository)
            .methodName("countByUserIds")
            .arguments(listOf(userIds))
            .pageSize(10)
            .sorts(sortedMapOf())
            .name("perfectDayCountReader")
            .build()
    }

    @StepScope
    @Bean
    fun perfectDayAchievementProcessor(
        @Value("#{stepExecutionContext['perfectDayAchievements']}") perfectDayAchievements: List<Achievement>
    ): FunctionItemProcessor<PerfectDayCount, AchievementAchieveLog> {
        return FunctionItemProcessor {
            val targetAchievement = perfectDayAchievements.firstOrNull { achievement -> it.count.toInt() == achievement.targetValue }
            targetAchievement?.let { target -> AchievementAchieveLog(target, it.userId) }
        }
    }

    @StepScope
    @Bean
    fun perfectDayAchievementWriter(
        achievementAchieveLogRepository: AchievementAchieveLogRepository
    ): RepositoryItemWriter<AchievementAchieveLog> {
        return RepositoryItemWriterBuilder<AchievementAchieveLog>()
            .repository(achievementAchieveLogRepository)
            .build()
    }
}