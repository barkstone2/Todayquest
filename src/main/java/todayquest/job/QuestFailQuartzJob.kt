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
class QuestFailQuartzJob(
    private val jobLauncher: JobLauncher? = null,
    private val questFailBatchJob: org.springframework.batch.core.Job? = null,
) : Job {
    private val log = LoggerFactory.getLogger(javaClass)

    override fun execute(context: JobExecutionContext) {

        val resetTime = LocalTime.of(LocalTime.now().hour, 0, 0)
        var targetDate = LocalDateTime.of(LocalDate.now(), resetTime)

        var jobParameters = JobParametersBuilder()
            .addString("targetDate", targetDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
            .addString("resetTime", resetTime.format(DateTimeFormatter.ISO_LOCAL_TIME))
            .toJobParameters()

        try {
            jobLauncher?.run(questFailBatchJob!!, jobParameters)
        } catch (_: JobInstanceAlreadyCompleteException) {
            log.info("-> {} 에 대한 중복 배치 작업이 발생했습니다.", targetDate)
        }
    }
}