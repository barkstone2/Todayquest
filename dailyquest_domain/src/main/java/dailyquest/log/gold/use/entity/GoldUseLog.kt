package dailyquest.log.gold.use.entity

import dailyquest.common.CreatedTimeEntity
import dailyquest.user.entity.User
import jakarta.persistence.*

@Table(name = "gold_use_log")
@Entity
class GoldUseLog(
    user: User,
    amount: Int,
    source: GoldUseSource
): CreatedTimeEntity() {
    @Column(name = "gold_use_log_id")
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User = user

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val source: GoldUseSource = source

    @Column(nullable = false)
    val amount: Int = amount
}