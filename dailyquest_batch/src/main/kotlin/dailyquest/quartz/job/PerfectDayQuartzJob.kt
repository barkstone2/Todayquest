package dailyquest.quartz.job

import org.quartz.Job
import org.quartz.JobExecutionContext
import org.slf4j.LoggerFactory
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class PerfectDayQuartzJob(
    private val jobLauncher: JobLauncher,
    private val perfectDayBatchJob: org.springframework.batch.core.Job,
) : Job {
    private val log = LoggerFactory.getLogger(javaClass)

    override fun execute(context: JobExecutionContext) {

        val loggedDate = LocalDate.now().minusDays(1)
        val jobParameters = JobParametersBuilder()
            .addLocalDate("loggedDate", loggedDate)
            .toJobParameters()

        try {
            jobLauncher.run(perfectDayBatchJob, jobParameters)
        } catch (_: JobInstanceAlreadyCompleteException) {
            log.info("[Duplicated Batch: PerfectDayJob] -> {} 완벽한 하루 배치 작업이 중복으로 발생했습니다.", loggedDate)
        }
    }
}