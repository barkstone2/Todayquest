package dailyquest.user.record.repository

import dailyquest.user.record.entity.UserRecord
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface BatchUserRecordRepository : UserRecordRepository, BatchUserRecordRepositoryCustom {
    fun findAllByIdIn(id: List<Long>, pageable: Pageable): Page<UserRecord>
}