package dailyquest.user.record.repository

import dailyquest.user.record.entity.UserRecord
import org.springframework.data.jpa.repository.JpaRepository

interface UserRecordRepository : JpaRepository<UserRecord, Long>