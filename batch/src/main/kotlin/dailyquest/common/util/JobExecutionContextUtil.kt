package dailyquest.common.util

import org.springframework.batch.core.JobExecution

@Suppress("UNCHECKED_CAST")
class JobExecutionContextUtil private constructor(
    private val jobExecution: JobExecution
) {
    @Throws(ClassCastException::class)
    fun <T> getFromJobContext(key: String): T? {
        return jobExecution.executionContext.get(key) as? T
    }

    fun putToJobContext(key: String, value: Any) {
        jobExecution.executionContext.put(key, value)
    }

    companion object {
        @JvmStatic
        fun from(jobExecution: JobExecution): JobExecutionContextUtil {
            val executionContextUtil = JobExecutionContextUtil(jobExecution)
            return executionContextUtil
        }
    }
}