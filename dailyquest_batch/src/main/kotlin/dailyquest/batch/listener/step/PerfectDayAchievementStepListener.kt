package dailyquest.batch.listener.step

import dailyquest.achievement.entity.AchievementAchieveLog
import dailyquest.achievement.entity.AchievementType
import dailyquest.achievement.repository.AchievementRepository
import dailyquest.common.util.ExecutionContextUtil
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
class PerfectDayAchievementStepListener(
    private val achievementRepository: AchievementRepository
) {
    private lateinit var executionContextUtil: ExecutionContextUtil
    private val perfectDayAchievementsKey = "perfectDayAchievements"
    private val achievedLogsKey = "achievedLogs"

    @BeforeStep
    fun beforeStep(stepExecution: StepExecution) {
        executionContextUtil = ExecutionContextUtil.from(stepExecution)
        val perfectDayAchievements = achievementRepository.getAllByTypeOrderByTargetValue(AchievementType.PERFECT_DAY)
        executionContextUtil.putToStepContext(perfectDayAchievementsKey, perfectDayAchievements)
    }

    @AfterStep
    fun afterStep(stepExecution: StepExecution): ExitStatus {
        executionContextUtil.removeFromStepContext(achievedLogsKey)
        executionContextUtil.removeFromStepContext(perfectDayAchievementsKey)
        return stepExecution.exitStatus
    }

    @AfterWrite
    fun afterWrite(chunk: Chunk<AchievementAchieveLog>) {
        val achieveLogsToCommit = chunk.toList()
        executionContextUtil.putToStepContext(achievedLogsKey, achieveLogsToCommit)
    }

    @AfterChunk
    fun afterChunk(context: ChunkContext) {
        if (context.isComplete) {
            executionContextUtil.mergeListFromStepContextToJobContext<AchievementAchieveLog>(achievedLogsKey)
        }
        executionContextUtil.removeFromStepContext(achievedLogsKey)
    }
}