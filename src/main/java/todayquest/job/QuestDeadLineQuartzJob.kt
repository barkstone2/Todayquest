package todayquest.job

import org.quartz.Job
import org.quartz.JobExecutionContext
import org.slf4j.LoggerFactory
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Component
class QuestDeadLineQuartzJob(
    private val jobLauncher: JobLauncher,
    private val questDeadLineBatchJob: org.springframework.batch.core.Job,
) : Job {
    private val log = LoggerFactory.getLogger(javaClass)

    override fun execute(context: JobExecutionContext) {

        val targetDate = LocalDateTime.now()

        val jobParameters = JobParametersBuilder()
            .addString("targetDate", targetDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
            .toJobParameters()

        try {
            jobLauncher.run(questDeadLineBatchJob, jobParameters)
        } catch (_: JobInstanceAlreadyCompleteException) {
            log.info("-> {} 에 대한 중복 배치 작업이 발생했습니다.", targetDate)
        }
    }
}