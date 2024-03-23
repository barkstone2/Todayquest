package dailyquest.batch.listener.step

import dailyquest.achievement.entity.AchievementAchieveLog
import dailyquest.common.util.ExecutionContextUtil
import org.springframework.batch.core.StepExecution
import org.springframework.batch.core.annotation.AfterWrite
import org.springframework.batch.core.annotation.BeforeStep
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.item.Chunk
import org.springframework.stereotype.Component

@StepScope
@Component
class PerfectDayLogStepListener {
    private lateinit var executionContextUtil: ExecutionContextUtil
    private val userIdsKey = "perfectDayLogUserIds"

    @BeforeStep
    fun beforeStep(stepExecution: StepExecution) {
        executionContextUtil = ExecutionContextUtil.from(stepExecution)
    }

    @AfterWrite
    fun afterWrite(chunk: Chunk<AchievementAchieveLog>) {
        val newUserIds = chunk.map { it.userId }.toList()
        val userIds = this.getUserIdsFromJobContext()
        userIds.addAll(newUserIds)
        executionContextUtil.putToJobContext(userIdsKey, userIds)
    }

    private fun getUserIdsFromJobContext(): MutableList<Long> {
        return executionContextUtil.getFromJobContext(userIdsKey) ?: mutableListOf()
    }
}