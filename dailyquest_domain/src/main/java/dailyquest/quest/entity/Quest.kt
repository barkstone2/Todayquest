package dailyquest.quest.entity

import dailyquest.common.BaseTimeEntity
import dailyquest.user.entity.UserInfo
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
class Quest(
    title: String,
    description: String = "",
    user: UserInfo,
    seq: Long,
    state: QuestState = QuestState.PROCEED,
    type: QuestType,
    deadline: LocalDateTime? = null
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    val user: UserInfo = user

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

    fun replaceDetailQuests(detailQuests: List<DetailQuest>?) {
        _detailQuests.clear()
        _detailQuests.addAll(detailQuests ?: emptyList())
    }

    fun updateQuestEntity(title: String, description: String = "", deadLine: LocalDateTime?, details: List<DetailQuest> = emptyList()) {
        this.title = title
        this.description = description
        this.deadLine = deadLine
        replaceDetailQuests(details)
    }

    /**
     * 퀘스트가 [QuestState.PROCEED] 상태인 경우 퀘스트를 완료 상태로 변경한다.
     * @return 변경에 성공하면 [QuestState.COMPLETE]가 반환된다.
     * 현재 퀘스트의 상태 변경이 불가능하다면, 현재 퀘스트의 상태가 반환된다.
     * 세부 퀘스트가 완료되지 않은 상태라면 현재 상태인 [QuestState.PROCEED] 상태가 반환된다.
     */
    fun completeQuest(): QuestState {
        if(state == QuestState.PROCEED && canComplete()) {
            state = QuestState.COMPLETE
        }
        return state
    }

    fun deleteQuest() {
        state = QuestState.DELETE
    }

    /**
     * 퀘스트가 [QuestState.PROCEED] 상태인 경우 퀘스트를 포기 상태로 변경한다.
     * @return 변경에 성공하면 [QuestState.DISCARD]가 반환된다.
     * 현재 퀘스트의 상태 변경이 불가능하다면, 현재 퀘스트의 상태가 반환된다.
     */
    fun discardQuest(): QuestState {
        if(state == QuestState.PROCEED) {
            state = QuestState.DISCARD
        }
        return state
    }

    fun failQuest() {
        state = QuestState.FAIL
    }

    fun isProceed(): Boolean {
        return state == QuestState.PROCEED
    }

    fun isQuestOfUser(userId: Long): Boolean {
        return user.id == userId
    }

    fun canComplete(): Boolean {
        return detailQuests.stream()
            .allMatch(DetailQuest::isCompletedDetailQuest)
    }

    fun isMainQuest(): Boolean {
        return type == QuestType.MAIN
    }

    /**
     * 세부 퀘스트의 카운트 값을 변경하거나 1 증가시킨다.
     * @param [detailQuestId] 변경할 세부 퀘스트의 ID를 나타냄.
     * @param [count] 변경할 카운트 값을 나타내며 null 값을 지정할 경우 1 증가함.
     * @return [DetailQuest] 변경된 세부 퀘스트 객체를 반환함. ID에 일치하는 세부 퀘스트가 없는 경우 null을 반환.
     */
    fun interactWithDetailQuest(detailQuestId: Long, count: Int?): DetailQuest? {
        val detailQuest = _detailQuests.firstOrNull { it.id == detailQuestId }
            ?: return null

        detailQuest.interact(count)
        return detailQuest
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
