package dailyquest.achievement.entity

import dailyquest.achievement.dto.AchievementUpdateRequest
import dailyquest.common.BaseTimeEntity
import jakarta.persistence.*
import java.io.Serializable

@Table(name = "achievement", uniqueConstraints = [UniqueConstraint(name = "unique_type_target_value", columnNames = ["type", "target_value"])])
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

    @Column(name = "target_value", nullable = false, updatable = false)
    val targetValue: Long = targetValue

    @Column(nullable = false)
    var inactivated: Boolean = false

    @Version
    private val version: Int = 0

    fun canAchieve(currentValue: Long): Boolean {
        return targetValue <= currentValue
    }

    fun updateAchievement(achievementUpdateRequest: AchievementUpdateRequest) {
        this.title = achievementUpdateRequest.title
        this.description = achievementUpdateRequest.description
    }

    fun activateAchievement() {
        this.inactivated = false
    }

    fun inactivateAchievement() {
        this.inactivated = true
    }
}