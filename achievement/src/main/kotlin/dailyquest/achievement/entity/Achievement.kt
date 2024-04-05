package dailyquest.achievement.entity

import dailyquest.common.BaseTimeEntity
import jakarta.persistence.*
import java.io.Serializable

@Entity
class Achievement(
    title: String = "",
    description: String = "",
    type: AchievementType,
    targetValue: Int,
): BaseTimeEntity(), Serializable {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "achievement_id")
    val id: Long = 0

    @Column(nullable = false)
    val title: String = title

    @Column(nullable = false)
    val description: String = description

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val type: AchievementType = type

    @Column(nullable = false)
    val targetValue: Int = targetValue

    fun canAchieve(currentValue: Long): Boolean {
        return this.type != AchievementType.EMPTY && targetValue <= currentValue
    }

    companion object {
        @JvmStatic
        fun empty(): Achievement {
            return Achievement(type = AchievementType.EMPTY, targetValue = 0)
        }
    }
}