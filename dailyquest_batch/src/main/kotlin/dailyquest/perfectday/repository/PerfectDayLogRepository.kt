package dailyquest.perfectday.repository

import dailyquest.log.perfectday.entity.PerfectDayLog
import dailyquest.perfectday.dto.PerfectDayCount
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface PerfectDayLogRepository: JpaRepository<PerfectDayLog, Long> {
    @Query("select new dailyquest.perfectday.dto.PerfectDayCount(pdl.userId, count(pdl.loggedDate)) from PerfectDayLog pdl where pdl.userId in (:userIds) group by pdl.userId")
    fun countByUserIds(@Param("userIds") userIds: List<Long>): List<PerfectDayCount>
}