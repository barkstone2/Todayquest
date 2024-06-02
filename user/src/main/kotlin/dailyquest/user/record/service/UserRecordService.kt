package dailyquest.user.record.service

import dailyquest.user.record.entity.UserRecord
import dailyquest.user.record.repository.UserRecordRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Transactional(readOnly = true)
@Service
class UserRecordService(
    private val userRecordRepository: UserRecordRepository
) {
    @Transactional
    fun saveNewRecordEntity(userId: Long) {
        val userRecord = UserRecord(userId)
        userRecordRepository.save(userRecord)
    }
}