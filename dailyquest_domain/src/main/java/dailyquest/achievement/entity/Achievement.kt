package dailyquest.achievement.entity

import dailyquest.common.BaseTimeEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id

@Entity
class Achievement(
    title: String = "",
    description: String = "",
    type: AchievementType,
    targetValue: Int,
): BaseTimeEntity() {

    @Id @GeneratedValue
    @Column(name = "achievement_id")
    val id: Long = 0

    @Column(nullable = false)
    val title: String = title

    @Column(nullable = false)
    val description: String = description

    @Column(nullable = false)
    val type: AchievementType = type

    @Column(nullable = false)
    val targetValue: Int = targetValue

    fun canAchieve(currentValue: Int): Boolean {
        return this.type != AchievementType.EMPTY && targetValue <= currentValue
    }

    companion object {
        @JvmStatic
        fun empty(): Achievement {
            return Achievement(type = AchievementType.EMPTY, targetValue = 0)
        }
    }
}