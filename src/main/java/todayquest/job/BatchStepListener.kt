package todayquest.job

import org.springframework.batch.core.annotation.AfterWrite
import org.springframework.batch.item.Chunk
import org.springframework.stereotype.Component
import todayquest.quest.entity.Quest
import todayquest.quest.entity.QuestLog
import todayquest.quest.repository.QuestLogRepository

@Component
class BatchStepListener(
    private val questLogRepository: QuestLogRepository,
) {

    @AfterWrite
    fun saveQuestLog(items : Chunk<Quest>) {
        questLogRepository.saveAll(items.items.map(::QuestLog))
    }
}