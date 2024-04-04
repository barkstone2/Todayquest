package dailyquest.log.gold.earn.repository

import dailyquest.log.gold.earn.entity.GoldEarnLog
import dailyquest.log.gold.earn.entity.GoldEarnSource
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface GoldEarnLogRepository: JpaRepository<GoldEarnLog, Long> {

    @Query("select coalesce(sum(gel.amount), 0) from GoldEarnLog gel where gel.userId = :userId")
    fun getTotalGoldEarnOfUser(@Param("userId") userId: Long): Int

    @Query("select coalesce(sum(gel.amount), 0) from GoldEarnLog gel where gel.userId = :userId and gel.source = :source")
    fun getTotalGoldEarnOfUserFromSource(@Param("userId") userId: Long, @Param("source") source: GoldEarnSource): Int
}