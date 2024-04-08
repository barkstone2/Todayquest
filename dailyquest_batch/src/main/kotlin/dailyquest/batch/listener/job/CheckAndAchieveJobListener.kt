package dailyquest.batch.listener.job

import dailyquest.achievement.repository.AchievementRepository
import dailyquest.common.util.JobExecutionContextUtil
import dailyquest.common.util.WebApiUtil
import dailyquest.properties.BatchContextProperties
import dailyquest.properties.BatchParameterProperties
import org.springframework.batch.core.BatchStatus
import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.annotation.AfterJob
import org.springframework.batch.core.annotation.BeforeJob
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component

@Component
class CheckAndAchieveJobListener(
    private val achievementRepository: AchievementRepository,
    private val batchParameterProperties: BatchParameterProperties,
    private val batchContextProperties: BatchContextProperties,
    private val webApiUtil: WebApiUtil
) {
    private lateinit var executionContextUtil: JobExecutionContextUtil

    @BeforeJob
    fun beforeJob(jobExecution: JobExecution) {
        executionContextUtil = JobExecutionContextUtil.from(jobExecution)
        val targetAchievementId = jobExecution.jobParameters.getLong(batchParameterProperties.targetAchievementIdKey)
        val targetAchievement = achievementRepository.findByIdOrNull(targetAchievementId)!!
        executionContextUtil.putToJobContext(batchContextProperties.targetAchievementKey, targetAchievement)
    }

    @AfterJob
    fun afterJob(jobExecution: JobExecution) {
        if (jobExecution.status == BatchStatus.COMPLETED) {
            val userIds = executionContextUtil.getFromJobContext<List<Long>>(batchContextProperties.notifiedUserIdsKey)
            if (userIds?.isNotEmpty() == true) {
                webApiUtil.postSseNotify(userIds)
            }
        }
    }
}