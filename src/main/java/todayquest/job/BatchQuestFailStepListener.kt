package todayquest.job

import org.slf4j.LoggerFactory
import org.springframework.batch.core.annotation.AfterWrite
import org.springframework.batch.core.annotation.OnProcessError
import org.springframework.batch.core.annotation.OnReadError
import org.springframework.batch.core.annotation.OnWriteError
import org.springframework.batch.core.configuration.annotation.JobScope
import org.springframework.batch.item.Chunk
import org.springframework.stereotype.Component
import todayquest.quest.entity.Quest
import todayquest.quest.entity.QuestLog
import todayquest.quest.repository.QuestLogRepository

@JobScope
@Component
class BatchQuestFailStepListener(
    private val questLogRepository: QuestLogRepository,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @AfterWrite
    fun saveQuestLog(items : Chunk<Quest>) {
        questLogRepository.saveAll(items.items.map(::QuestLog))
    }

    @OnReadError
    fun onReadError(e: Exception) {
        log.error("[Batch Exception]", e)
    }

    @OnWriteError
    fun onWriteError(e: Exception, items : Chunk<Quest>) {
        log.error("[Batch Exception]", e)
    }

    @OnProcessError
    fun onProcessError(quest: Quest, e: Exception) {
        log.error("[Batch Exception]", e)
    }


}