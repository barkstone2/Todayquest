package dailyquest.log.perfectday.entity

import jakarta.persistence.*
import java.time.LocalDate

@Table(name = "perfect_day_log")
@Entity
class PerfectDayLog(
    userId: Long,
    loggedDate: LocalDate
) {
    @Column(name = "perfect_day_log_id")
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0
    @Column(nullable = false, updatable = false)
    val userId: Long = userId
    @Column(nullable = false, updatable = false)
    val loggedDate: LocalDate = loggedDate
}