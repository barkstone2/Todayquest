package dailyquest.config

import dailyquest.batch.job.BatchQuestFailStepListener
import dailyquest.quest.entity.Quest
import dailyquest.quest.repository.QuestRepository
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.batch.core.configuration.annotation.JobScope
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.core.job.builder.JobBuilder
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
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter


@EnableBatchProcessing
@Configuration
class BatchConfig(
    private val jobRepository: JobRepository,
    private val transactionManager: PlatformTransactionManager,
    private val questRepository: QuestRepository,
    private val batchQuestFailStepListener: BatchQuestFailStepListener,
) {

    @Bean
    fun questResetBatchJob(
        questResetBatchStep: Step
    ): Job {
        return JobBuilder("questResetJob", jobRepository)
            .start(questResetBatchStep)
            .build()
    }

    @Bean
    fun questDeadLineBatchJob(
        questDeadLineBatchStep: Step
    ): Job {
        return JobBuilder("questDeadLineJob", jobRepository)
            .start(questDeadLineBatchStep)
            .build()
    }

    @Bean
    @JobScope
    fun questResetBatchStep(
        questResetReader: RepositoryItemReader<Quest>,
        questFailProcessor: FunctionItemProcessor<Quest, Quest>,
        questWriter: RepositoryItemWriter<Quest>
    ): Step {
        return StepBuilder("questResetStep", jobRepository)
            .chunk<Quest, Quest>(10, transactionManager)
            .reader(questResetReader)
            .processor(questFailProcessor)
            .writer(questWriter)
            .listener(batchQuestFailStepListener)
            .faultTolerant()
            .retryLimit(3)
            .retry(Exception::class.java)
            .build()
    }

    @Bean
    @JobScope
    fun questDeadLineBatchStep(
        questDeadLineReader: RepositoryItemReader<Quest>,
        questFailProcessor: FunctionItemProcessor<Quest, Quest>,
        questWriter: RepositoryItemWriter<Quest>
    ): Step {
        return StepBuilder("questDeadLineStep", jobRepository)
            .chunk<Quest, Quest>(10, transactionManager)
            .reader(questDeadLineReader)
            .processor(questFailProcessor)
            .writer(questWriter)
            .listener(batchQuestFailStepListener)
            .faultTolerant()
            .retryLimit(3)
            .retry(Exception::class.java)
            .build()
    }

    @Bean
    @StepScope
    fun questResetReader(@Value("#{jobParameters[resetTime]}") resetTimeStr: String): RepositoryItemReader<Quest> {

        return RepositoryItemReaderBuilder<Quest>()
            .repository(questRepository)
            .methodName("getQuestsForResetBatch")
            .arguments(LocalTime.parse(resetTimeStr))
            .pageSize(10)
            .sorts(sortedMapOf())
            .name("questResetReader")
            .build()
    }

    @Bean
    @StepScope
    fun questDeadLineReader(@Value("#{jobParameters[targetDate]}") targetDateStr: String): RepositoryItemReader<Quest> {
        return RepositoryItemReaderBuilder<Quest>()
            .repository(questRepository)
            .methodName("getQuestForDeadLineBatch")
            .arguments(LocalDateTime.parse(targetDateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
            .pageSize(10)
            .sorts(sortedMapOf())
            .name("questDeadLineReader")
            .build()
    }

    @Bean
    @StepScope
    fun questFailProcessor(): FunctionItemProcessor<Quest, Quest> {
        return FunctionItemProcessor { quest ->
            quest.failQuest()
            quest
        }
    }

    @Bean
    @StepScope
    fun questWriter(): RepositoryItemWriter<Quest> {
        return RepositoryItemWriterBuilder<Quest>()
            .repository(questRepository)
            .build()
    }

}