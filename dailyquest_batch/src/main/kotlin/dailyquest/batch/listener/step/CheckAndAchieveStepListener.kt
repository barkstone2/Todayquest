package dailyquest.batch.listener.step

import dailyquest.achievement.entity.AchievementAchieveLog
import dailyquest.common.util.ExecutionContextUtil
import dailyquest.properties.BatchContextProperties
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
class CheckAndAchieveStepListener(
    private val batchContextProperties: BatchContextProperties
) {
    private lateinit var executionContextUtil: ExecutionContextUtil

    @BeforeStep
    fun beforeStep(stepExecution: StepExecution) {
        executionContextUtil = ExecutionContextUtil.from(stepExecution)
    }

    @AfterStep
    fun afterStep(stepExecution: StepExecution): ExitStatus {
        executionContextUtil.removeFromStepContext(batchContextProperties.achievedLogsKey)
        return stepExecution.exitStatus
    }

    @AfterWrite
    fun afterWrite(chunk: Chunk<AchievementAchieveLog>) {
        val achieveLogsToCommit = chunk.toList()
        executionContextUtil.putToStepContext(batchContextProperties.achievedLogsKey, achieveLogsToCommit)
    }

    @AfterChunk
    fun afterChunk(context: ChunkContext) {
        if (context.isComplete) {
            executionContextUtil.mergeListFromStepContextToJobContext<AchievementAchieveLog>(batchContextProperties.achievedLogsKey)
        }
        executionContextUtil.removeFromStepContext(batchContextProperties.achievedLogsKey)
    }
}