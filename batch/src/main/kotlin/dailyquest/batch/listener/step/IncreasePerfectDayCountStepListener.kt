package dailyquest.batch.listener.step

import dailyquest.common.util.ExecutionContextUtil
import dailyquest.user.dto.UserPerfectDayCount
import dailyquest.user.entity.User
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

class IncreasePerfectDayCountStepListener {
    private lateinit var executionContextUtil: ExecutionContextUtil
    private val userPerfectDayCountsKey = "userPerfectDayCounts"

    @BeforeStep
    fun beforeStep(stepExecution: StepExecution) {
        executionContextUtil = ExecutionContextUtil.from(stepExecution)
    }

    @AfterStep
    fun afterStep(stepExecution: StepExecution): ExitStatus {
        executionContextUtil.removeFromStepContext(userPerfectDayCountsKey)
        return stepExecution.exitStatus
    }

    @AfterWrite
    fun afterWrite(chunk: Chunk<User>) {
        val userPerfectDayCounts = chunk.map { UserPerfectDayCount.from(it) }
        executionContextUtil.putToStepContext(userPerfectDayCountsKey, userPerfectDayCounts)
    }

    @AfterChunk
    fun afterChunk(context: ChunkContext) {
        if (context.isComplete) {
            executionContextUtil.mergeListFromStepContextToJobContext<UserPerfectDayCount>(userPerfectDayCountsKey)
        }
        executionContextUtil.removeFromStepContext(userPerfectDayCountsKey)
    }
}