package dailyquest.batch.listener.step

import dailyquest.common.util.ExecutionContextUtil
import dailyquest.notification.entity.Notification
import org.springframework.batch.core.StepExecution
import org.springframework.batch.core.annotation.AfterWrite
import org.springframework.batch.core.annotation.BeforeStep
import org.springframework.batch.item.Chunk
import org.springframework.stereotype.Component

@Component
class AchievementNotificationStepListener {
    private lateinit var executionContextUtil: ExecutionContextUtil
    private val notifiedUserIdsKey = "notifiedUserIds"

    @BeforeStep
    fun beforeStep(stepExecution: StepExecution) {
        executionContextUtil = ExecutionContextUtil.from(stepExecution)
    }

    @AfterWrite
    fun afterWrite(chunk: Chunk<Notification>) {
        val newNotifiedUserIds = chunk.map { it.userId }.toList()
        val notifiedUserIds = this.getNotifiedUserIdsFromJobContext()
        notifiedUserIds.addAll(newNotifiedUserIds)
        executionContextUtil.putToJobContext(notifiedUserIdsKey, notifiedUserIds)
    }

    private fun getNotifiedUserIdsFromJobContext(): MutableList<Long> {
        return executionContextUtil.getFromJobContext(notifiedUserIdsKey) ?: mutableListOf()
    }
}