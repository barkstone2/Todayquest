package dailyquest.batch.step

import dailyquest.batch.listener.step.ReadPerfectDayUserIdStepListener
import dailyquest.quest.repository.QuestLogRepository
import dailyquest.user.entity.User
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.JobScope
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.item.data.RepositoryItemReader
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder
import org.springframework.batch.item.support.ListItemWriter
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager
import java.time.LocalDate

@Configuration(proxyBeanMethods = false)
class ReadPerfectDayUserIdStepConfig {
    @Bean
    @JobScope
    fun readPerfectDayUserIdStep(
        jobRepository: JobRepository,
        transactionManager: PlatformTransactionManager,
        perfectDayUserIdReader: RepositoryItemReader<Long>,
        perfectDayLogStepListener: ReadPerfectDayUserIdStepListener
    ): Step {
        return StepBuilder("readPerfectDayUserIdStep", jobRepository)
            .chunk<Long, User>(10, transactionManager)
            .reader(perfectDayUserIdReader)
            .writer(ListItemWriter())
            .listener(perfectDayLogStepListener)
            .faultTolerant()
            .retryLimit(3)
            .retry(Exception::class.java)
            .build()
    }

    @Bean
    @StepScope
    fun perfectDayUserIdReader(
        @Value("#{jobParameters[loggedDate]}") loggedDate: LocalDate,
        questLogRepository: QuestLogRepository
    ): RepositoryItemReader<Long> {
        return RepositoryItemReaderBuilder<Long>()
            .repository(questLogRepository)
            .methodName("getAllUserIdsWhoAchievedPerfectDay")
            .arguments(loggedDate)
            .pageSize(10)
            .name("perfectDayUserIdReader")
            .sorts(sortedMapOf())
            .build()
    }
}