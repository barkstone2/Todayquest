package todayquest.quest.entity

import jakarta.persistence.*
import org.springframework.security.access.AccessDeniedException
import todayquest.common.BaseTimeEntity
import todayquest.common.MessageUtil
import todayquest.quest.dto.*
import todayquest.user.entity.UserInfo

@Entity
class Quest(
    title: String,
    description: String?,
    user: UserInfo,
    seq: Long,
    state: QuestState = QuestState.PROCEED,
    type: QuestType,
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

    @OneToMany(mappedBy = "quest", cascade = [CascadeType.ALL], orphanRemoval = true)
    private val _detailQuests: MutableList<DetailQuest> = mutableListOf()
    val detailQuests : List<DetailQuest>
        get() = _detailQuests.toList()

    fun updateQuestEntity(dto: QuestRequest) {
        title = dto.title
        description = dto.description
    }

    fun updateDetailQuests(detailQuestRequestDtos: List<DetailQuestRequestDto>): List<DetailQuest> {
        val newDetailQuests: MutableList<DetailQuestRequestDto> = mutableListOf()

        val updateCount = detailQuestRequestDtos.size
        for (i in 0 until updateCount) {
            val newDetailQuest = detailQuestRequestDtos[i]
            try {
                _detailQuests[i].updateDetailQuest(newDetailQuest)
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

        return newDetailQuests.map { it.mapToEntity(this) }
            .toCollection(mutableListOf())
    }

    fun completeQuest() {
        require(state != QuestState.DELETE) { MessageUtil.getMessage("quest.error.deleted") }
        require(state == QuestState.PROCEED) { MessageUtil.getMessage("quest.error.not-proceed") }
        require(
            detailQuests.stream()
                .allMatch(DetailQuest::isCompletedDetailQuest)
        ) { MessageUtil.getMessage("quest.error.complete.detail") }

        state = QuestState.COMPLETE
    }

    fun deleteQuest() {
        state = QuestState.DELETE
    }

    fun discardQuest() {
        require(state != QuestState.DELETE) { MessageUtil.getMessage("quest.error.deleted") }
        state = QuestState.DISCARD
    }

    fun failQuest() {
        state = QuestState.FAIL
    }

    fun checkIsProceedingQuest() {
        require(state == QuestState.PROCEED) { MessageUtil.getMessage("quest.error.update.invalid.state") }
    }

    fun checkIsQuestOfValidUser(userId: Long) {
        if (user.id != userId) throw AccessDeniedException(
            MessageUtil.getMessage(
                "exception.access.denied",
                MessageUtil.getMessage("quest")
            )
        )

    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Quest

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }


}
