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
import todayquest.job.BatchStepListener
import todayquest.quest.entity.Quest
import todayquest.quest.entity.QuestState
import todayquest.quest.repository.QuestRepository
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter


@EnableBatchProcessing
@Configuration
class BatchConfig(
    private val jobRepository: JobRepository,
    private val transactionManager: PlatformTransactionManager,
    private val questRepository: QuestRepository,
    private val batchStepListener: BatchStepListener,
) {

    @Bean
    fun questFailBatchJob(): Job {
        return JobBuilder("questFailJob", jobRepository)
            .start(questFailBatchStep())
            .build()
    }

    @Bean
    @JobScope
    fun questFailBatchStep(): Step {
        val step = StepBuilder("questFailStep", jobRepository)
            .chunk<Quest, Quest>(10, transactionManager)
            .reader(questReader())
            .processor(questProcessor())
            .writer(questWriter())
            .listener(batchStepListener)
            .build()
        step.isAllowStartIfComplete = true
        return step
    }

    @Bean
    @StepScope
    fun questProcessor(): ItemProcessor<in Quest, out Quest> {
        return ItemProcessor { quest: Quest ->
            quest.failQuest()
            quest
        }
    }

    @Bean
    @StepScope
    fun questReader(): ItemReader<out Quest> {

        val jobExecution = JobSynchronizationManager.getContext().jobExecution
        val jobParameters = jobExecution.jobParameters

        val targetDate = LocalDateTime.parse(jobParameters.getString("targetDate"), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        val resetTime = LocalTime.parse(jobParameters.getString("resetTime"))

        return RepositoryItemReaderBuilder<Quest>()
            .repository(questRepository)
            .methodName("getQuestsForBatch")
            .arguments(QuestState.PROCEED, targetDate, resetTime)
            .pageSize(10)
            .sorts(mapOf(Pair("id", Sort.Direction.ASC)))
            .name("questFailTargetReader")
            .build()
    }

    @Bean
    @StepScope
    fun questWriter(): ItemWriter<in Quest> {
        return RepositoryItemWriterBuilder<Quest>()
            .repository(questRepository)
            .build()
    }

}