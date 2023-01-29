package todayquest.quest.entity

import todayquest.quest.dto.DetailQuestRequestDto
import jakarta.persistence.*
import jakarta.persistence.EnumType.*
import jakarta.persistence.FetchType.*
import jakarta.persistence.GenerationType.IDENTITY

@Entity
@Table(name = "detail_quest")
class DetailQuest(
    title: String,
    targetCount: Short,
    type: DetailQuestType,
    state: DetailQuestState,
    quest: Quest,
) {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "detail_quest_id")
    val id: Long? = null

    @Column(nullable = false, length = 100)
    var title: String = title
        protected set

    @Column(nullable = false)
    var targetCount: Short = targetCount
        protected set

    @Column(nullable = false)
    var count: Short = 0
        protected set

    @Enumerated(STRING)
    @Column(nullable = false)
    var type: DetailQuestType = type
        protected set

    @Enumerated(STRING)
    @Column(nullable = false)
    var state: DetailQuestState = state
        protected set

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "quest_id")
    val quest: Quest = quest

    fun updateDetailQuest(newDetailQuest: DetailQuestRequestDto) {
        title = newDetailQuest.title!!
        type = newDetailQuest.type!!
        targetCount = if (newDetailQuest.type == DetailQuestType.COUNT) newDetailQuest.targetCount!! else 1

        if(newDetailQuest.id != this.id || newDetailQuest.type != this.type) resetCount()

        if(count < targetCount) state = DetailQuestState.PROCEED
        else {
            state = DetailQuestState.COMPLETE
            count = targetCount
        }

    }

    fun resetCount() {
        count = 0
        state = DetailQuestState.PROCEED
    }

    fun changeState(state: DetailQuestState?) {
        this.state = state!!
    }

    fun addCount() {
        count++
        if (count == targetCount) changeState(DetailQuestState.COMPLETE)
    }

    fun changeCount(count: Short) {
        this.count = if(count > targetCount) targetCount else count

        when {
            count < targetCount -> changeState(DetailQuestState.PROCEED)
            else -> changeState(DetailQuestState.COMPLETE)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DetailQuest

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }

}