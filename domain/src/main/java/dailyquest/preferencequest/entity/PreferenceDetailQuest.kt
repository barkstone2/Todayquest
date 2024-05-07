package dailyquest.preferencequest.entity

import dailyquest.quest.entity.DetailQuestType
import jakarta.persistence.*

@Table(name = "preference_detail_quest")
@Entity
class PreferenceDetailQuest private constructor(
    title: String,
    targetCount: Int,
    type: DetailQuestType,
) {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "preference_detail_quest_id")
    val id: Long = 0

    @Column(nullable = false, length = 300)
    var title: String = title
        protected set

    @Column(nullable = false)
    var targetCount: Int = targetCount
        protected set

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var type: DetailQuestType = type
        protected set

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "preference_quest_id")
    var preferenceQuest: PreferenceQuest? = null
        protected set

    fun linkToParent(preferenceQuest: PreferenceQuest) {
        this.preferenceQuest = preferenceQuest
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PreferenceDetailQuest

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    companion object {
        @JvmStatic
        fun of(
            title: String,
            targetCount: Int,
            type: DetailQuestType,
        ): PreferenceDetailQuest {
            return PreferenceDetailQuest(title, targetCount, type)
        }
    }
}