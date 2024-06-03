package dailyquest.batch.step

import dailyquest.achievement.entity.Achievement
import dailyquest.achievement.entity.AchievementAchieveLog
import dailyquest.achievement.repository.AchievementAchieveLogRepository
import dailyquest.batch.listener.step.PerfectDayAchievementStepListener
import dailyquest.user.dto.UserPerfectDayCount
import dailyquest.user.repository.UserRepository
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.JobScope
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.item.ItemProcessor
import org.springframework.batch.item.ItemReader
import org.springframework.batch.item.ItemWriter
import org.springframework.batch.item.data.RepositoryItemWriter
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder
import org.springframework.batch.item.function.FunctionItemProcessor
import org.springframework.batch.item.support.IteratorItemReader
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
        perfectDayCountReader: ItemReader<UserPerfectDayCount>,
        perfectDayAchievementProcessor: ItemProcessor<UserPerfectDayCount, AchievementAchieveLog>,
        perfectDayAchievementWriter: ItemWriter<AchievementAchieveLog>,
        perfectDayAchievementStepListener: PerfectDayAchievementStepListener
    ): Step {
        return StepBuilder("perfectDayAchievementStep", jobRepository)
            .chunk<UserPerfectDayCount, AchievementAchieveLog>(10, transactionManager)
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
        @Value("#{jobExecutionContext['userPerfectDayCounts']}") userPerfectDayCounts: List<UserPerfectDayCount>
    ): IteratorItemReader<UserPerfectDayCount> {
        return IteratorItemReader(userPerfectDayCounts)
    }

    @StepScope
    @Bean
    fun perfectDayAchievementProcessor(
        @Value("#{stepExecutionContext['perfectDayAchievements']}") perfectDayAchievements: List<Achievement>
    ): FunctionItemProcessor<UserPerfectDayCount, AchievementAchieveLog> {
        return FunctionItemProcessor {
            val targetAchievement = perfectDayAchievements.firstOrNull { achievement -> achievement.canAchieve(it.perfectDayCount.toLong()) }
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