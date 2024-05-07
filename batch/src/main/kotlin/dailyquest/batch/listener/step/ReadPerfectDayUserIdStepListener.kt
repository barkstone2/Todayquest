package dailyquest.batch.listener.step

import dailyquest.common.util.ExecutionContextUtil
import org.springframework.batch.core.ExitStatus
import org.springframework.batch.core.StepExecution
import org.springframework.batch.core.annotation.AfterChunk
import org.springframework.batch.core.annotation.AfterStep
import org.springframework.batch.core.annotation.AfterWrite
import org.springframework.batch.core.annotation.BeforeStep
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.batch.item.Chunk
import org.springframework.stereotype.Component

@StepScope
@Component
class ReadPerfectDayUserIdStepListener {
    private lateinit var executionContextUtil: ExecutionContextUtil
    private val userIdsKey = "perfectDayLogUserIds"

    @BeforeStep
    fun beforeStep(stepExecution: StepExecution) {
        executionContextUtil = ExecutionContextUtil.from(stepExecution)
    }

    @AfterStep
    fun afterStep(stepExecution: StepExecution): ExitStatus {
        executionContextUtil.removeFromStepContext(userIdsKey)
        return stepExecution.exitStatus
    }

    @AfterWrite
    fun afterWrite(chunk: Chunk<Long>) {
        val userIdsToCommit = chunk.toList()
        executionContextUtil.putToStepContext(userIdsKey, userIdsToCommit)
    }

    @AfterChunk
    fun afterChunk(context: ChunkContext) {
        if (context.isComplete) {
            executionContextUtil.mergeListFromStepContextToJobContext<Long>(userIdsKey)
        }
        executionContextUtil.removeFromStepContext(userIdsKey)
    }
}