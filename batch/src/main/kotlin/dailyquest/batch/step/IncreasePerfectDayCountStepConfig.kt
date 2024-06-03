package dailyquest.batch.step

import dailyquest.batch.listener.step.IncreasePerfectDayCountStepListener
import dailyquest.user.record.entity.UserRecord
import dailyquest.user.record.repository.BatchUserRecordRepository
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
        perfectDayUserReader: RepositoryItemReader<UserRecord>,
        perfectDayCountIncreaseProcessor: FunctionItemProcessor<UserRecord, UserRecord>,
        increasePerfectDayCountStepListener: IncreasePerfectDayCountStepListener,
        userWriter: RepositoryItemWriter<UserRecord>,
    ): Step {
        return StepBuilder("perfectDayLogStep", jobRepository)
            .chunk<UserRecord, UserRecord>(10, transactionManager)
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
        userRecordRepository: BatchUserRecordRepository
    ): RepositoryItemReader<UserRecord> {
        return RepositoryItemReaderBuilder<UserRecord>()
            .repository(userRecordRepository)
            .methodName("findAllByIdIn")
            .arguments(listOf(userIds))
            .pageSize(10)
            .name("perfectDayUserReader")
            .sorts(sortedMapOf())
            .build()
    }

    @Bean
    @StepScope
    fun perfectDayCountIncreaseProcessor(): FunctionItemProcessor<UserRecord, UserRecord> {
        return FunctionItemProcessor { user ->
            user.increasePerfectDayCount()
            user
        }
    }

    @Bean
    @StepScope
    fun userWriter(userRecordRepository: BatchUserRecordRepository): RepositoryItemWriter<UserRecord> {
        return RepositoryItemWriterBuilder<UserRecord>()
            .repository(userRecordRepository)
            .build()
    }
}