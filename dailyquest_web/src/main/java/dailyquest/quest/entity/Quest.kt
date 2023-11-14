package dailyquest.quest.entity

import jakarta.persistence.*
import org.springframework.security.access.AccessDeniedException
import dailyquest.common.BaseTimeEntity
import dailyquest.common.MessageUtil
import dailyquest.quest.dto.*
import dailyquest.user.entity.UserInfo
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

    fun updateQuestEntity(dto: QuestRequest) {
        title = dto.title
        description = dto.description
        deadLine = dto.deadLine
        updateDetailQuests(dto.details)
    }

    fun updateDetailQuests(detailRequests: List<DetailRequest>) {
        val newDetailQuests: MutableList<DetailQuest> = mutableListOf()

        val updateCount = detailRequests.size
        for (i in 0 until updateCount) {
            val newDetailQuest = detailRequests[i]
            try {
                _detailQuests[i].updateDetailQuest(newDetailQuest)
            } catch (e: IndexOutOfBoundsException) {
                newDetailQuests.add(newDetailQuest.mapToEntity(this))
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

    fun completeQuest() {
        check(state != QuestState.DELETE) { MessageUtil.getMessage("quest.error.deleted") }
        check(state == QuestState.PROCEED) { MessageUtil.getMessage("quest.error.not-proceed") }
        check(canComplete()) { MessageUtil.getMessage("quest.error.complete.detail") }

        state = QuestState.COMPLETE
    }

    fun deleteQuest() {
        state = QuestState.DELETE
    }

    fun discardQuest() {
        check(state != QuestState.DELETE) { MessageUtil.getMessage("quest.error.deleted") }
        check(state == QuestState.PROCEED) { MessageUtil.getMessage("quest.error.not-proceed") }

        state = QuestState.DISCARD
    }

    fun failQuest() {
        state = QuestState.FAIL
    }

    fun checkStateIsProceedOrThrow() {
        check(state == QuestState.PROCEED) { MessageUtil.getMessage("quest.error.not-proceed") }
    }

    fun checkOwnershipOrThrow(userId: Long) {
        if (user.id != userId)
            throw AccessDeniedException(MessageUtil.getMessage("exception.access.denied"))
    }

    fun canComplete(): Boolean {
        return detailQuests.stream()
            .allMatch(DetailQuest::isCompletedDetailQuest)
    }

    fun isMainQuest(): Boolean {
        return type == QuestType.MAIN
    }

    fun interactWithDetailQuest(detailQuestId: Long, request: DetailInteractRequest? = null): DetailResponse {
        val detailQuest = _detailQuests.firstOrNull { it.id == detailQuestId }
            ?: throw IllegalStateException(MessageUtil.getMessage("exception.badRequest"))

        checkStateIsProceedOrThrow()

        if(request != null) {
            detailQuest.changeCount(request.count)
            return DetailResponse.createDto(detailQuest, canComplete())
        }

        if(detailQuest.isCompletedDetailQuest()) {
            detailQuest.resetCount()
            return DetailResponse.createDto(detailQuest)
        }

        detailQuest.addCount()

        return DetailResponse.createDto(detailQuest, canComplete())
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
