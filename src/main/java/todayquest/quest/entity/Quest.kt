package todayquest.quest.entity

import todayquest.common.BaseTimeEntity
import todayquest.quest.dto.DetailQuestRequestDto
import todayquest.quest.dto.QuestRequestDto
import todayquest.reward.entity.Reward
import todayquest.user.entity.UserInfo
import java.time.LocalDate
import java.time.LocalTime
import jakarta.persistence.*

@Entity
class Quest(
    title: String,
    description: String?,
    user: UserInfo,
    seq: Long,
    state: QuestState = QuestState.PROCEED,
    difficulty: QuestDifficulty,
    type: QuestType,
) : BaseTimeEntity() {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "quest_id")
    val id: Long? = null

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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var difficulty: QuestDifficulty = difficulty
        protected set

    // 퀘스트에서 제거되면 매핑 엔티티 삭제
    @OneToMany(mappedBy = "quest", cascade = [CascadeType.ALL], orphanRemoval = true)
    private val _rewards: MutableList<QuestReward> = mutableListOf()
    val rewards : List<QuestReward>
        get() = _rewards.toList()

    @OneToMany(mappedBy = "quest", cascade = [CascadeType.ALL], orphanRemoval = true)
    private val _detailQuests: MutableList<DetailQuest> = mutableListOf()
    val detailQuests : List<DetailQuest>
        get() = _detailQuests.toList()

    fun updateQuestEntity(dto: QuestRequestDto, updateRewards: List<Reward>): MutableList<QuestReward> {
        title = dto.title ?: throw IllegalArgumentException("퀘스트 이름은 비어있을 수 없습니다.")
        description = dto.description
        difficulty = dto.difficulty
        return updateRewardList(updateRewards)
    }

    private fun updateRewardList(updateRewards: List<Reward>): MutableList<QuestReward> {
        val newRewards: MutableList<QuestReward> = mutableListOf()
        val updateCount = updateRewards.size

        for (i in 0 until updateCount) {
            val newReward = QuestReward(reward = updateRewards[i], quest = this)
            try {
                _rewards[i].updateReward(updateRewards[i])
            } catch (e: IndexOutOfBoundsException) {
                newRewards.add(newReward)
            }
        }

        val overCount: Int = _rewards.size - updateCount
        if (overCount > 0) {
            for (i in updateCount until updateCount + overCount) {
                // 새로 변경된 rewards의 길이 index에서 요소를 계속 삭제
                _rewards.removeAt(updateCount)
            }
        }
        return newRewards
    }

    fun changeState(state: QuestState) {
        this.state = state
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

}
