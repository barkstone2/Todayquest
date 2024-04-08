package dailyquest.achievement.entity

import dailyquest.common.CreatedTimeEntity
import jakarta.persistence.*
import java.io.Serializable

@Table(name = "achievement_achieve_log")
@Entity
class AchievementAchieveLog(
    achievement: Achievement,
    userId: Long,
): CreatedTimeEntity(), Serializable {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "achievement_achieve_log_id")
    val id: Long = 0

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "achievement_id")
    val achievement: Achievement = achievement

    @Column(name = "user_id", nullable = false, updatable = false)
    val userId: Long = userId

    companion object {
        @JvmStatic
        fun of(achievement: Achievement, userId: Long): AchievementAchieveLog {
            return AchievementAchieveLog(achievement, userId)
        }
    }

}