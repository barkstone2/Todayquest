package dailyquest.batch.step

import dailyquest.batch.listener.step.IncreasePerfectDayCountStepListener
import dailyquest.user.entity.User
import dailyquest.user.repository.UserRepository
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.JobScope
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.item.data.RepositoryItemReader
import org.springframework.batch.item.data.RepositoryItemWriter
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder
import org.springframework.batch.item.function.FunctionItemProcessor
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager

@Configuration
class IncreasePerfectDayCountStepConfig {
    @Bean
    @JobScope
    fun increasePerfectDayCountStep(
        jobRepository: JobRepository,
        transactionManager: PlatformTransactionManager,
        perfectDayUserReader: RepositoryItemReader<User>,
        perfectDayCountIncreaseProcessor: FunctionItemProcessor<User, User>,
        increasePerfectDayCountStepListener: IncreasePerfectDayCountStepListener,
        userWriter: RepositoryItemWriter<User>,
    ): Step {
        return StepBuilder("perfectDayLogStep", jobRepository)
            .chunk<User, User>(10, transactionManager)
            .reader(perfectDayUserReader)
            .processor(perfectDayCountIncreaseProcessor)
            .writer(userWriter)
            .listener(increasePerfectDayCountStepListener)
            .faultTolerant()
            .retryLimit(3)
            .retry(Exception::class.java)
            .build()
    }

    @Bean
    @StepScope
    fun perfectDayUserReader(
        @Value("#{jobExecutionContext['perfectDayLogUserIds']}") userIds: List<Long>,
        userRepository: UserRepository
    ): RepositoryItemReader<User> {
        return RepositoryItemReaderBuilder<User>()
            .repository(userRepository)
            .methodName("findAllByIdIn")
            .arguments(listOf(userIds))
            .pageSize(10)
            .name("perfectDayUserReader")
            .sorts(sortedMapOf())
            .build()
    }

    @Bean
    @StepScope
    fun perfectDayCountIncreaseProcessor(): FunctionItemProcessor<User, User> {
        return FunctionItemProcessor { user ->
            user.increasePerfectDayCount()
            user
        }
    }

    @Bean
    @StepScope
    fun userWriter(userRepository: UserRepository): RepositoryItemWriter<User> {
        return RepositoryItemWriterBuilder<User>()
            .repository(userRepository)
            .build()
    }
}