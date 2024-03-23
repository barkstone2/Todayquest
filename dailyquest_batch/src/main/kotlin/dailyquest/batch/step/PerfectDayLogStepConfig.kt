package dailyquest.batch.step

import dailyquest.batch.listener.step.PerfectDayLogStepListener
import dailyquest.log.perfectday.entity.PerfectDayLog
import dailyquest.perfectday.repository.PerfectDayLogRepository
import dailyquest.quest.repository.QuestLogRepository
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
import java.time.LocalDate

@Configuration(proxyBeanMethods = false)
class PerfectDayLogStepConfig {
    @Bean
    @JobScope
    fun perfectDayLogStep(
        jobRepository: JobRepository,
        transactionManager: PlatformTransactionManager,
        perfectDayUserReader: RepositoryItemReader<Long>,
        perfectDayLogProcessor: FunctionItemProcessor<Long, PerfectDayLog>,
        perfectDayLogWriter: RepositoryItemWriter<PerfectDayLog>,
        perfectDayLogStepListener: PerfectDayLogStepListener
    ): Step {
        return StepBuilder("perfectDayLogStep", jobRepository)
            .chunk<Long, PerfectDayLog>(10, transactionManager)
            .reader(perfectDayUserReader)
            .processor(perfectDayLogProcessor)
            .writer(perfectDayLogWriter)
            .listener(perfectDayLogStepListener)
            .faultTolerant()
            .retryLimit(3)
            .retry(Exception::class.java)
            .build()
    }

    @Bean
    @StepScope
    fun perfectDayUserReader(
        @Value("#{jobParameters[loggedDate]}") loggedDate: LocalDate,
        questLogRepository: QuestLogRepository
    ): RepositoryItemReader<Long> {
        return RepositoryItemReaderBuilder<Long>()
            .repository(questLogRepository)
            .methodName("getAllUserIdsWhoAchievedPerfectDay")
            .arguments(loggedDate)
            .pageSize(10)
            .name("perfectDayUserReader")
            .build()
    }

    @Bean
    @StepScope
    fun perfectDayLogProcessor(
        @Value("#{jobParameters[loggedDate]}") loggedDate: LocalDate,
    ): FunctionItemProcessor<Long, PerfectDayLog> {
        return FunctionItemProcessor { userId ->
            PerfectDayLog(userId, loggedDate)
        }
    }

    @Bean
    @StepScope
    fun perfectDayLogWriter(perfectDayLogRepository: PerfectDayLogRepository): RepositoryItemWriter<PerfectDayLog> {
        return RepositoryItemWriterBuilder<PerfectDayLog>()
            .repository(perfectDayLogRepository)
            .build()
    }
}