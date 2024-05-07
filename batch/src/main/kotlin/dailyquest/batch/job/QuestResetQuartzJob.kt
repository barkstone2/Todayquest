package dailyquest.batch.job

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
class QuestResetQuartzJob (
    private val jobLauncher: JobLauncher,
    private val questResetBatchJob: org.springframework.batch.core.Job,
) : Job {
    private val log = LoggerFactory.getLogger(javaClass)

    override fun execute(context: JobExecutionContext) {

        val resetDate = LocalDate.now()
        val resetDateTime = LocalDateTime.of(resetDate, LocalTime.of(6, 0))
        val jobParameters = JobParametersBuilder()
            .addString("resetDateTime", resetDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
            .toJobParameters()

        try {
            jobLauncher.run(questResetBatchJob, jobParameters)
        } catch (_: JobInstanceAlreadyCompleteException) {
            log.info("[Duplicated Batch: QuestResetJob] -> {} 퀘스트 초기화 배치 작업이 중복으로 발생했습니다.", resetDate.minusDays(1))
        }
    }
}