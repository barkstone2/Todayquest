package dailyquest.preferencequest.entity

import dailyquest.common.BaseTimeEntity
import dailyquest.quest.entity.Quest
import dailyquest.user.entity.UserInfo
import jakarta.persistence.*
import java.time.LocalDateTime

@Table(name = "preference_quest")
@Entity
class PreferenceQuest(
    title: String,
    description: String?,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    val user: UserInfo,

    var deadLine: LocalDateTime? = null,
) : BaseTimeEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "preference_quest_id")
    val id: Long = 0

    @Column(length = 300, nullable = false)
    var title: String = title
        protected set

    @Column(length = 3000)
    var description: String? = description
        protected set

    var deletedDate: LocalDateTime? = null
        protected set

    @OneToMany(mappedBy = "preference_quest", cascade = [CascadeType.ALL], orphanRemoval = true)
    private val _preferenceDetailQuests: MutableList<PreferenceDetailQuest> = mutableListOf()
    val preferenceDetailQuests : List<PreferenceDetailQuest>
        get() = _preferenceDetailQuests.toList()

    fun updatePreferenceQuest(title: String, description: String?, deadLine: LocalDateTime?, details: List<Pair<Long?, PreferenceDetailQuest>>?) {
        this.title = title
        this.description = description
        this.deadLine = deadLine
        updateDetailQuests(details ?: emptyList())
    }

    fun updateDetailQuests(preferenceDetailRequests: List<Pair<Long?, PreferenceDetailQuest>>) {
        val newPreferenceDetailQuests: MutableList<PreferenceDetailQuest> = mutableListOf()

        val updateCount = preferenceDetailRequests.size
        for (i in 0 until updateCount) {
            val id = preferenceDetailRequests[i].first
            val newDetailQuest = preferenceDetailRequests[i].second
            try {
                _preferenceDetailQuests[i].updatePreferenceDetailQuest(id, newDetailQuest)
            } catch (e: IndexOutOfBoundsException) {
                newPreferenceDetailQuests.add(newDetailQuest)
            }
        }

        val overCount: Int = _preferenceDetailQuests.size - updateCount
        if (overCount > 0) {
            for (i in updateCount until updateCount + overCount) {
                _preferenceDetailQuests.removeAt(updateCount)
            }
        }

        _preferenceDetailQuests.addAll(newPreferenceDetailQuests)
    }

    fun deletePreferenceQuest() {
        deletedDate = LocalDateTime.now()
    }

    fun isPreferenceQuestOfUser(userId: Long): Boolean {
        return user.id == userId
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Quest

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

}
