package todayquest.quest.entity

import todayquest.reward.entity.Reward
import javax.persistence.*

/**
 * 퀘스트 - 리워드 다대다 매핑 엔티티
 * 추후에 리워드 엔티티에 유저 정보가 들어가지 않는 경우가 발생할 예정이라 추가 -> 파티 리워드
 */
@Entity
@Table(name = "quest_reward")
class QuestReward(
    quest: Quest,
    reward: Reward,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "quest_reward_id")
    val id: Long? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quest_id")
    val quest: Quest = quest

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reward_id")
    var reward: Reward = reward
        protected set

    fun updateReward(reward: Reward) {
        this.reward = reward
    }

}