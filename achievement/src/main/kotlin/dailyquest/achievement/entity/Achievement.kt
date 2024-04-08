package dailyquest.achievement.entity

import dailyquest.achievement.dto.AchievementUpdateRequest
import dailyquest.common.BaseTimeEntity
import jakarta.persistence.*
import java.io.Serializable

@Entity
class Achievement(
    title: String = "",
    description: String = "",
    type: AchievementType,
    targetValue: Long,
): BaseTimeEntity(), Serializable {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "achievement_id")
    val id: Long = 0

    @Column(nullable = false)
    var title: String = title
        protected set

    @Column(nullable = false)
    var description: String = description
        protected set

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    val type: AchievementType = type

    @Column(nullable = false, updatable = false)
    val targetValue: Long = targetValue

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = true

    fun canAchieve(currentValue: Long): Boolean {
        return this.type != AchievementType.EMPTY && targetValue <= currentValue
    }

    fun updateAchievement(achievementUpdateRequest: AchievementUpdateRequest) {
        this.title = achievementUpdateRequest.title
        this.description = achievementUpdateRequest.description
    }

    fun activateAchievement() {
        this.isActive = true
    }

    fun inactivateAchievement() {
        this.isActive = false
    }

    companion object {
        @JvmStatic
        fun empty(): Achievement {
            return Achievement(type = AchievementType.EMPTY, targetValue = 0)
        }
    }
}