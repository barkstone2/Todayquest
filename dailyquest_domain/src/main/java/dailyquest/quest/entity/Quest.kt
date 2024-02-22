package dailyquest.quest.entity

import dailyquest.common.BaseTimeEntity
import dailyquest.user.entity.UserInfo
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
class Quest(
    title: String,
    description: String?,
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
    var description: String? = description
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

    fun updateQuestEntity(title: String, description: String?, deadLine: LocalDateTime?, details: List<Pair<Long?, DetailQuest>>?) {
        this.title = title
        this.description = description
        this.deadLine = deadLine
        updateDetailQuests(details ?: emptyList())
    }

    /**
     * 퀘스트 저장 시에 이 메서드를 후속 호출 해 세부 퀘스트를 업데이트 해야 한다.
     * 퀘스트 수정 시에는 이 메서드를 호출해서는 안 된다.
     * @param [detailRequests] 새로운 세부 퀘스트 목록으로 세부 퀘스트 ID와 엔티티의 [Pair]<[Long]?, [DetailQuest]> 목록이다.
     */
    fun updateDetailQuests(detailRequests: List<Pair<Long?, DetailQuest>>) {
        val newDetailQuests: MutableList<DetailQuest> = mutableListOf()

        val updateCount = detailRequests.size
        for (i in 0 until updateCount) {
            val id = detailRequests[i].first
            val newDetailQuest = detailRequests[i].second
            try {
                _detailQuests[i].updateDetailQuest(id, newDetailQuest)
            } catch (e: IndexOutOfBoundsException) {
                newDetailQuests.add(newDetailQuest)
            }
        }

        val overCount: Int = _detailQuests.size - updateCount
        if (overCount > 0) {
            for (i in updateCount until updateCount + overCount) {
                _detailQuests.removeAt(updateCount)
            }
        }

        _detailQuests.addAll(newDetailQuests)
    }

    /**
     * 퀘스트가 [QuestState.PROCEED] 상태인 경우 퀘스트를 완료 상태로 변경한다.
     * @return 변경에 성공하면 [QuestState.COMPLETE]가 반환된다.
     * 현재 퀘스트의 상태 변경이 불가능하다면, 현재 퀘스트의 상태가 반환된다.
     * 세부 퀘스트가 완료되지 않은 상태라면 현재 상태인 [QuestState.PROCEED] 상태가 반환된다.
     */
    fun completeQuest(): QuestState {
        if(isProceed() && canComplete()) {
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
        if(isProceed()) {
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
