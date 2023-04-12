package todayquest.config

import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.batch.core.configuration.annotation.JobScope
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.scope.context.JobSynchronizationManager
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.item.ItemProcessor
import org.springframework.batch.item.ItemReader
import org.springframework.batch.item.ItemWriter
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.domain.Sort
import org.springframework.transaction.PlatformTransactionManager
import todayquest.job.BatchQuestFailStepListener
import todayquest.quest.entity.Quest
import todayquest.quest.entity.QuestState
import todayquest.quest.repository.QuestRepository
import java.lang.Exception
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
    fun questResetBatchJob(): Job {
        return JobBuilder("questResetJob", jobRepository)
            .start(questResetBatchStep())
            .build()
    }

    @Bean
    fun questDeadLineBatchJob(): Job {
        return JobBuilder("questDeadLineJob", jobRepository)
            .start(questDeadLineBatchStep())
            .build()
    }

    @Bean
    @JobScope
    fun questResetBatchStep(): Step {
        val step = StepBuilder("questResetStep", jobRepository)
            .chunk<Quest, Quest>(10, transactionManager)
            .reader(questResetReader())
            .processor(questFailProcessor())
            .writer(questWriter())
            .listener(batchQuestFailStepListener)
            .faultTolerant()
            .retryLimit(3)
            .retry(Exception::class.java)
            .build()
        step.isAllowStartIfComplete = true
        return step
    }

    @Bean
    @JobScope
    fun questDeadLineBatchStep(): Step {
        val step = StepBuilder("questDeadLineStep", jobRepository)
            .chunk<Quest, Quest>(10, transactionManager)
            .reader(questDeadLineReader())
            .processor(questFailProcessor())
            .writer(questWriter())
            .listener(batchQuestFailStepListener)
            .build()
        step.isAllowStartIfComplete = true
        return step
    }

    @Bean
    @StepScope
    fun questResetReader(): ItemReader<out Quest> {

        val jobExecution = JobSynchronizationManager.getContext()?.jobExecution!!
        val jobParameters = jobExecution.jobParameters

        val resetTime = LocalTime.parse(jobParameters.getString("resetTime"))

        return RepositoryItemReaderBuilder<Quest>()
            .repository(questRepository)
            .methodName("getQuestsForResetBatch")
            .arguments(QuestState.PROCEED, resetTime)
            .pageSize(10)
            .sorts(mapOf(Pair("id", Sort.Direction.ASC)))
            .name("questResetReader")
            .build()
    }

    @Bean
    @StepScope
    fun questDeadLineReader(): ItemReader<out Quest> {

        val jobExecution = JobSynchronizationManager.getContext()?.jobExecution!!
        val jobParameters = jobExecution.jobParameters

        val targetDate = LocalDateTime.parse(jobParameters.getString("targetDate"), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

        return RepositoryItemReaderBuilder<Quest>()
            .repository(questRepository)
            .methodName("getQuestForDeadLineBatch")
            .arguments(QuestState.PROCEED, targetDate)
            .pageSize(10)
            .sorts(mapOf(Pair("id", Sort.Direction.ASC)))
            .name("questDeadLineReader")
            .build()
    }

    @Bean
    @StepScope
    fun questFailProcessor(): ItemProcessor<in Quest, out Quest> {
        return ItemProcessor { quest: Quest ->
            quest.failQuest()
            quest
        }
    }

    @Bean
    @StepScope
    fun questWriter(): ItemWriter<in Quest> {
        return RepositoryItemWriterBuilder<Quest>()
            .repository(questRepository)
            .build()
    }

}