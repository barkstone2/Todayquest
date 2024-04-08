package dailyquest.batch.step

import dailyquest.achievement.entity.Achievement
import dailyquest.achievement.entity.AchievementAchieveLog
import dailyquest.achievement.entity.AchievementType.*
import dailyquest.achievement.repository.AchievementAchieveLogRepository
import dailyquest.batch.listener.step.CheckAndAchieveStepListener
import dailyquest.user.entity.User
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
        checkAndAchieveReader: ItemReader<User>,
        checkAndAchieveProcessor: ItemProcessor<User, AchievementAchieveLog>,
        checkAndAchieveWriter: ItemWriter<AchievementAchieveLog>,
        checkAndAchieveStepListener: CheckAndAchieveStepListener
    ): Step {
        return StepBuilder("checkAndAchieveStep", jobRepository)
            .chunk<User, AchievementAchieveLog>(10, transactionManager)
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
        batchUserRepository: BatchUserRepository,
    ): RepositoryItemReader<User> {
        var builder = RepositoryItemReaderBuilder<User>()
            .repository(batchUserRepository)
            .name("checkAndAchieveReader")
            .sorts(sortedMapOf())
        builder = when (targetAchievement.type) {
            QUEST_REGISTRATION -> builder.methodName("findAllByQuestRegistrationCountGreaterThanEqual")
            QUEST_COMPLETION -> builder.methodName("findAllByQuestCompletionCountGreaterThanEqual")
            QUEST_CONTINUOUS_REGISTRATION_DAYS -> builder.methodName("findAllByMaxQuestContinuousRegistrationDaysGreaterThanEqual")
            QUEST_CONTINUOUS_COMPLETION -> builder.methodName("findAllByMaxQuestContinuousCompletionDaysGreaterThanEqual")
            GOLD_EARN -> builder.methodName("findAllByGoldEarnAmountGreaterThanEqual")
            PERFECT_DAY -> builder.methodName("findAllByPerfectDayCountGreaterThanEqual")
            USER_LEVEL, EMPTY -> throw IllegalArgumentException("지원하지 않는 타입의 업적입니다.")
        }
        return builder.arguments(targetAchievement.targetValue).build()
    }

    @Bean
    @StepScope
    fun checkAndAchieveProcessor(
        @Value("#{jobExecutionContext['targetAchievement']}") targetAchievement: Achievement,
    ): FunctionItemProcessor<User, AchievementAchieveLog> {
        return FunctionItemProcessor {
            AchievementAchieveLog.of(targetAchievement, it.id)
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