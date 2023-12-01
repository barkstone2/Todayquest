package dailyquest.batch.job

import dailyquest.quest.entity.Quest
import dailyquest.quest.entity.QuestLog
import dailyquest.quest.entity.QuestState
import dailyquest.quest.repository.QuestLogRepository
import dailyquest.search.document.QuestDocument
import dailyquest.search.repository.QuestIndexRepository
import org.slf4j.LoggerFactory
import org.springframework.batch.core.annotation.AfterWrite
import org.springframework.batch.core.annotation.OnProcessError
import org.springframework.batch.core.annotation.OnReadError
import org.springframework.batch.core.annotation.OnWriteError
import org.springframework.batch.item.Chunk
import org.springframework.stereotype.Component

@Component
class BatchQuestFailStepListener(
    private val questLogRepository: QuestLogRepository,
    private val questIndexRepository: QuestIndexRepository,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @AfterWrite
    fun afterWrite(items : Chunk<Quest>) {
        val questLogList = mutableListOf<QuestLog>();
        val documentList = mutableListOf<QuestDocument>();
        for (item in items.items) {
            questLogList.add(QuestLog(item))
            documentList.add(QuestDocument(
                item.id,
                item.title,
                item.description,
                item.detailQuests.map { it.title }.toList(),
                item.user.id,
                QuestState.FAIL.name,
                item.createdDate
            ))
        }

        questLogRepository.saveAll(questLogList)
        questIndexRepository.saveAll(documentList)
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