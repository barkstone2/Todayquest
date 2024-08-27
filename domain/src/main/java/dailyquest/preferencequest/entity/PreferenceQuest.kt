package dailyquest.preferencequest.entity

import dailyquest.common.BaseTimeEntity
import dailyquest.preferencequest.dto.PreferenceQuestRequest
import dailyquest.user.entity.User
import jakarta.persistence.*
import java.time.LocalDateTime

@Table(name = "preference_quest")
@Entity
class PreferenceQuest private constructor(
    title: String,
    description: String = "",
    user: User,
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    val user: User = user

    @OneToMany(mappedBy = "preferenceQuest", cascade = [CascadeType.ALL], orphanRemoval = true)
    private val _preferenceDetailQuests: MutableList<PreferenceDetailQuest> = mutableListOf()
    val preferenceDetailQuests : List<PreferenceDetailQuest>
        get() = _preferenceDetailQuests.toList()

    fun updatePreferenceQuest(requestDto: PreferenceQuestRequest) {
        this.title = requestDto.title
        this.description = requestDto.description
        replaceDetailQuests(requestDto.details.map { it.mapToEntity() })
    }

    private fun replaceDetailQuests(detailQuests: List<PreferenceDetailQuest>) {
        _preferenceDetailQuests.clear()
        _preferenceDetailQuests.addAll(detailQuests)
        detailQuests.forEach { it.linkToParent(this) }
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

    companion object {
        @JvmStatic
        fun of(
            title: String,
            description: String = "",
            details: List<PreferenceDetailQuest> = emptyList(),
            user: User
        ): PreferenceQuest {
            val preferenceQuest = PreferenceQuest(
                title,
                description,
                user
            )
            preferenceQuest.replaceDetailQuests(details)
            return preferenceQuest
        }
    }
}
