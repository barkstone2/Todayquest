package dailyquest.batch.listener.job

import dailyquest.common.util.JobExecutionContextUtil
import dailyquest.common.util.WebApiUtil
import org.springframework.batch.core.BatchStatus
import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.annotation.AfterJob
import org.springframework.batch.core.annotation.BeforeJob
import org.springframework.stereotype.Component

@Component
class PerfectDayJobListener(
    private val webApiUtil: WebApiUtil
) {
    private lateinit var executionContextUtil: JobExecutionContextUtil
    private val notifiedUserIdsKey = "notifiedUserIds"

    @BeforeJob
    fun beforeJob(jobExecution: JobExecution) {
        executionContextUtil = JobExecutionContextUtil.from(jobExecution)
    }

    @AfterJob
    fun afterJob(jobExecution: JobExecution) {
        if (jobExecution.status == BatchStatus.COMPLETED) {
            val userIds = executionContextUtil.getFromJobContext<List<Long>>(notifiedUserIdsKey)
            if (userIds?.isNotEmpty() == true) {
                webApiUtil.postSseNotify(userIds)
            }
        }
    }
}