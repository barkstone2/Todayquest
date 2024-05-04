package dailyquest.quest.entity

import dailyquest.common.BaseTimeEntity
import dailyquest.preferencequest.entity.PreferenceQuest
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
class Quest(
    title: String,
    description: String = "",
    userId: Long,
    seq: Long,
    state: QuestState = QuestState.PROCEED,
    type: QuestType,
    deadline: LocalDateTime? = null,
    preferenceQuest: PreferenceQuest? = null,
) : BaseTimeEntity() {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "quest_id")
    val id: Long = 0

    @Column(length = 50, nullable = false)
    var title: String = title
        protected set

    @Column(length = 300)
    var description: String = description
        protected set

    @Column(name = "user_id")
    val userId: Long = userId

    @Column(name = "user_quest_seq", nullable = false)
    val seq: Long = seq

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val type: QuestType = type

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var state: QuestState = state
        protected set

    var deadLine: LocalDateTime? = deadline

    @OneToMany(mappedBy = "quest", cascade = [CascadeType.ALL], orphanRemoval = true)
    private val _detailQuests: MutableList<DetailQuest> = mutableListOf()
    val detailQuests : List<DetailQuest>
        get() = _detailQuests.toList()

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "preference_quest_id")
    private val preferenceQuest: PreferenceQuest? = preferenceQuest

    fun replaceDetailQuests(detailQuests: List<DetailQuest>) {
        _detailQuests.clear()
        _detailQuests.addAll(detailQuests)
    }

    fun updateQuestEntity(title: String, description: String = "", deadLine: LocalDateTime?, details: List<DetailQuest> = emptyList()) {
        this.title = title
        this.description = description
        this.deadLine = deadLine
        replaceDetailQuests(details)
    }

    fun completeQuestIfPossible() {
        if (this.isProceed() && this.canComplete()) {
            state = QuestState.COMPLETE
        }
    }

    fun deleteQuest() {
        state = QuestState.DELETE
    }

    fun discardQuestIfPossible() {
        if (this.isProceed()) {
            state = QuestState.DISCARD
        }
    }

    fun failQuest() {
        state = QuestState.FAIL
    }

    fun isProceed(): Boolean {
        return state == QuestState.PROCEED
    }

    fun canComplete(): Boolean {
        return detailQuests.stream()
            .allMatch(DetailQuest::isCompleted)
    }

    fun isMainQuest(): Boolean {
        return type == QuestType.MAIN
    }

    fun updateDetailQuestCount(detailQuestId: Long, count: Int?): DetailQuest? {
        val detailQuest = _detailQuests.firstOrNull { it.id == detailQuestId } ?: return null
        return detailQuest.updateCountAndState(count)
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
