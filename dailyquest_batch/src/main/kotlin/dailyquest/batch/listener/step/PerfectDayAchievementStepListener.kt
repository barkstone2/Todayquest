package dailyquest.batch.listener.step

import dailyquest.achievement.entity.AchievementAchieveLog
import dailyquest.achievement.entity.AchievementType
import dailyquest.achievement.repository.AchievementRepository
import dailyquest.common.util.ExecutionContextUtil
import org.springframework.batch.core.StepExecution
import org.springframework.batch.core.annotation.AfterWrite
import org.springframework.batch.core.annotation.BeforeStep
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
        val perfectDayAchievements = achievementRepository.getAllByType(AchievementType.PERFECT_DAY)
        executionContextUtil.putToStepContext(perfectDayAchievementsKey, perfectDayAchievements)
    }

    @AfterWrite
    fun afterWrite(chunk: Chunk<AchievementAchieveLog>) {
        val newAchieveLogs = chunk.toList()
        val achieveLogs = this.getAchievedLogsFromJobContext()
        achieveLogs.addAll(newAchieveLogs)
        executionContextUtil.putToJobContext(achievedLogsKey, achieveLogs)
    }

    private fun getAchievedLogsFromJobContext(): MutableList<AchievementAchieveLog> {
        return executionContextUtil.getFromJobContext(achievedLogsKey) ?: mutableListOf()
    }
}