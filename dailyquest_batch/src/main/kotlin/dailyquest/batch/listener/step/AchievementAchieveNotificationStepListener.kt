package dailyquest.batch.listener.step

import dailyquest.achievement.entity.AchievementAchieveLog
import dailyquest.common.util.ExecutionContextUtil
import dailyquest.log.perfectday.entity.PerfectDayLog
import org.springframework.batch.core.ExitStatus
import org.springframework.batch.core.StepExecution
import org.springframework.batch.core.annotation.AfterChunk
import org.springframework.batch.core.annotation.AfterStep
import org.springframework.batch.core.annotation.AfterWrite
import org.springframework.batch.core.annotation.BeforeStep
import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.batch.item.Chunk
import org.springframework.stereotype.Component

@Component
class AchievementAchieveNotificationStepListener {
    private lateinit var executionContextUtil: ExecutionContextUtil
    private val notifiedUserIdsKey = "notifiedUserIds"

    @BeforeStep
    fun beforeStep(stepExecution: StepExecution) {
        executionContextUtil = ExecutionContextUtil.from(stepExecution)
    }

    @AfterStep
    fun afterStep(stepExecution: StepExecution): ExitStatus {
        executionContextUtil.removeFromStepContext(notifiedUserIdsKey)
        return stepExecution.exitStatus
    }

    @AfterWrite
    fun afterWrite(chunk: Chunk<AchievementAchieveLog>) {
        val newNotifiedUserIds = chunk.map { it.userId }.toList()
        executionContextUtil.putToStepContext(notifiedUserIdsKey, newNotifiedUserIds)
    }

    @AfterChunk
    fun afterChunk(context: ChunkContext) {
        if (context.isComplete) {
            executionContextUtil.mergeListFromStepContextToJobContext<Long>(notifiedUserIdsKey)
        }
        executionContextUtil.removeFromStepContext(notifiedUserIdsKey)
    }
}