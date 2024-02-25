package dailyquest.achievement.entity

import dailyquest.common.BaseTimeEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id

@Entity
class Achievement(
    type: AchievementType,
    targetValue: Int,
): BaseTimeEntity() {

    @Id @GeneratedValue
    @Column(name = "achievement_id")
    val id: Long = 0

    @Column(nullable = false)
    val type: AchievementType = type

    @Column(nullable = false)
    val targetValue: Int = targetValue

    fun canAchieve(currentValue: Int): Boolean {
        return targetValue <= currentValue
    }
}