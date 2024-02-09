package dailyquest.preferencequest.entity

import dailyquest.common.BaseTimeEntity
import dailyquest.user.entity.UserInfo
import jakarta.persistence.*
import java.time.LocalDateTime

@Table(name = "preference_quest")
@Entity
class PreferenceQuest(
    title: String,
    description: String = "",
    deadLine: LocalDateTime? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    val user: UserInfo,
) : BaseTimeEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "preference_quest_id")
    val id: Long = 0

    @Column(length = 300, nullable = false)
    var title: String = title
        protected set

    @Column(length = 3000)
    var description: String = description
        protected set

    var deletedDate: LocalDateTime? = null
        protected set

    var deadLine: LocalDateTime? = deadLine
        protected set

    @OneToMany(mappedBy = "preferenceQuest", cascade = [CascadeType.ALL], orphanRemoval = true)
    private val _preferenceDetailQuests: MutableList<PreferenceDetailQuest> = mutableListOf()
    val preferenceDetailQuests : List<PreferenceDetailQuest>
        get() = _preferenceDetailQuests.toList()

    fun updatePreferenceQuest(title: String, description: String = "", deadLine: LocalDateTime? = null, details: List<PreferenceDetailQuest> = emptyList()) {
        this.title = title
        this.description = description
        this.deadLine = deadLine
        replaceDetailQuests(details)
    }

    fun replaceDetailQuests(detailQuests: List<PreferenceDetailQuest>) {
        _preferenceDetailQuests.clear()
        _preferenceDetailQuests.addAll(detailQuests)
    }

    fun deletePreferenceQuest() {
        deletedDate = LocalDateTime.now()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PreferenceQuest

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

}
