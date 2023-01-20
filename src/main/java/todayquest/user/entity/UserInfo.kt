package todayquest.user.entity

import org.hibernate.annotations.DynamicInsert
import todayquest.common.BaseTimeEntity
import todayquest.quest.entity.QuestDifficulty
import javax.persistence.*

@DynamicInsert
@Entity
@Table(name = "user_info")
class UserInfo(
    oauth2Id: String,
    nickname: String,
    providerType: ProviderType,
) : BaseTimeEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    val id: Long? = null

    @Column(nullable = false)
    val oauth2Id: String = oauth2Id

    @Column(nullable = false, length = 20)
    var nickname: String = nickname
        protected set

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val providerType: ProviderType = providerType

    var level: Int = 1
        protected set
    var exp: Long = 0
        protected set
    var gold: Long = 0
        protected set

    fun updateNickname(nickname: String) {
        this.nickname = nickname
    }

    fun earnExpAndGold(clearInfo: QuestDifficulty, targetExp: Long) {
        gold += clearInfo.gold.toLong()
        exp += clearInfo.experience.toLong()
        levelUpCheck(targetExp)
    }

    private fun levelUpCheck(targetExp: Long) {
        if (level == 100) return
        if (exp >= targetExp) {
            level += 1
            exp -= targetExp
        }
    }

}