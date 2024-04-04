package dailyquest.log.gold.earn.entity

import dailyquest.common.CreatedTimeEntity
import jakarta.persistence.*

@Table(name = "gold_earn_log")
@Entity
class GoldEarnLog(
    userId: Long,
    amount: Long,
    source: GoldEarnSource
): CreatedTimeEntity() {
    @Column(name = "gold_earn_log_id")
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0

    @Column(name = "user_id", nullable = false)
    val userId: Long = userId

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val source: GoldEarnSource = source

    @Column(nullable = false)
    val amount: Long = amount
}