package dailyquest.quest.entity

import jakarta.persistence.*
import jakarta.persistence.EnumType.STRING
import jakarta.persistence.FetchType.LAZY
import jakarta.persistence.GenerationType.IDENTITY
import kotlin.math.min

@Entity
@Table(name = "detail_quest")
class DetailQuest private constructor(
    title: String,
    targetCount: Int,
    count: Int = 0,
    type: DetailQuestType,
    state: DetailQuestState,
    quest: Quest,
) {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "detail_quest_id")
    val id: Long = 0

    @Column(nullable = false, length = 100)
    var title: String = title
        protected set

    @Column(nullable = false)
    var targetCount: Int = targetCount
        protected set

    @Column(nullable = false)
    var count: Int = count
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

    fun updateDetailQuest(id: Long?, detailQuest: DetailQuest) {
        this.title = detailQuest.title
        this.type = detailQuest.type
        if(id != this.id || type != this.type) resetCount()
        updateTargetCountBasedOnType()
        updateCountAndStateBasedOnCount(detailQuest.count)
    }

    private fun updateTargetCountBasedOnType() {
        this.targetCount = when (this.type) {
            DetailQuestType.CHECK -> 1
            else -> targetCount
        }
    }

    fun updateCountAndState(count: Int?): DetailQuest {
        when (count) {
            null -> adjustCountBasedOnCompletion()
            else -> updateCountAndStateBasedOnCount(count)
        }
        return this
    }

    private fun adjustCountBasedOnCompletion() {
        when {
            this.isCompleted() -> resetCount()
            else -> addCount()
        }
    }

    private fun updateCountAndStateBasedOnCount(count: Int) {
        this.count = min(targetCount, count)
        this.state = decideStateBasedOnCount()
    }

    private fun decideStateBasedOnCount(): DetailQuestState {
        return when {
            count < targetCount -> DetailQuestState.PROCEED
            else -> DetailQuestState.COMPLETE
        }
    }

    fun isCompleted() : Boolean {
        return state == DetailQuestState.COMPLETE
    }

    fun resetCount() {
        count = 0
        state = DetailQuestState.PROCEED
    }

    fun addCount() {
        if (count < targetCount)
            count++
        if (count == targetCount)
            state = DetailQuestState.COMPLETE
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DetailQuest

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    companion object {
        @JvmStatic
        fun of(
            title: String,
            targetCount: Int,
            count: Int,
            type: DetailQuestType,
            state: DetailQuestState,
            quest: Quest,
        ): DetailQuest {
            val detailQuest = DetailQuest(title, targetCount, count, type, state, quest)
            detailQuest.updateTargetCountBasedOnType()
            return detailQuest
        }

        @JvmStatic
        fun of(
            title: String,
            targetCount: Int,
            type: DetailQuestType,
            state: DetailQuestState,
            quest: Quest,
        ): DetailQuest {
            val detailQuest = DetailQuest(title, targetCount, type = type, state = state, quest = quest)
            detailQuest.updateTargetCountBasedOnType()
            return detailQuest
        }
    }
}