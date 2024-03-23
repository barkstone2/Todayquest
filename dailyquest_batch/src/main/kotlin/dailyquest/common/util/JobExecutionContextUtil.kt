package dailyquest.common.util

import org.springframework.batch.core.JobExecution

class JobExecutionContextUtil(
    private val jobExecution: JobExecution
) {
    @Suppress("UNCHECKED_CAST")
    fun <T> getFromJobContext(key: String): T {
        return jobExecution.executionContext.get(key) as T
    }

    fun putToJobContext(key: String, value: Any) {
        jobExecution.executionContext.put(key, value)
    }
}