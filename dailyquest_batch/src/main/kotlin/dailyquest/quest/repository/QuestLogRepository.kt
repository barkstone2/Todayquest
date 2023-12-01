package dailyquest.quest.repository

import dailyquest.quest.entity.QuestLog
import org.springframework.data.jpa.repository.JpaRepository

interface QuestLogRepository : JpaRepository<QuestLog, Long>