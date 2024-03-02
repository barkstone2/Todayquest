package dailyquest.achievement.entity

import dailyquest.common.BaseTimeEntity
import dailyquest.user.entity.UserInfo
import jakarta.persistence.*

@Table(name = "achievement_achieve_log")
@Entity
class AchievementAchieveLog(
    achievement: Achievement,
    user: UserInfo,
): BaseTimeEntity() {

    @Id @GeneratedValue
    @Column(name = "achievement_achieve_log_id")
    val id: Long = 0

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "achievement_id")
    val achievement: Achievement = achievement

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    val user: UserInfo = user

    companion object {
        fun of(achievement: Achievement, user: UserInfo): AchievementAchieveLog {
            return AchievementAchieveLog(achievement, user)
        }
    }

}