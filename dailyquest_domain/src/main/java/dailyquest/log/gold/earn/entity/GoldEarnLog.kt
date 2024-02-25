package dailyquest.log.gold.earn.entity

import dailyquest.common.CreatedTimeEntity
import dailyquest.user.entity.UserInfo
import jakarta.persistence.*

@Table(name = "gold_earn_log")
@Entity
class GoldEarnLog(
    user: UserInfo,
    amount: Int,
    source: GoldEarnSource
): CreatedTimeEntity() {
    @Column(name = "gold_earn_log_id")
    @Id @GeneratedValue
    val id: Long = 0

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: UserInfo = user

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val source: GoldEarnSource = source

    @Column(nullable = false)
    val amount: Int = amount
}