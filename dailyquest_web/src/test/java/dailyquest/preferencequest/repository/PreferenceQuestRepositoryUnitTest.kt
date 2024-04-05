package dailyquest.preferencequest.repository

import dailyquest.config.JpaAuditingConfiguration
import dailyquest.preferencequest.entity.PreferenceQuest
import dailyquest.quest.entity.Quest
import dailyquest.quest.entity.QuestType
import dailyquest.quest.repository.QuestRepository
import dailyquest.user.entity.ProviderType
import dailyquest.user.entity.User
import dailyquest.user.repository.UserRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import

@DisplayName("선호 퀘스트 리포지토리 유닛 테스트")
@DataJpaTest
@Import(JpaAuditingConfiguration::class)
class PreferenceQuestRepositoryUnitTest {

    @Autowired
    lateinit var preferenceQuestRepository: PreferenceQuestRepository

    @Autowired
    lateinit var questRepository: QuestRepository

    @Autowired
    lateinit var userRepository: UserRepository

    var user: User = User("", "", ProviderType.GOOGLE)
    var anotherUser: User = User("", "", ProviderType.GOOGLE)

    @BeforeEach
    fun init() {
        user = if(user.id == 0L) userRepository.save(User("", "user1", ProviderType.GOOGLE)) else user
        anotherUser = if(anotherUser.id == 0L) userRepository.save(User("", "user2", ProviderType.GOOGLE)) else anotherUser
        anotherUser.updateCoreTime(0)
        userRepository.saveAndFlush(anotherUser)
    }

    @DisplayName("findAllWithUsedCount 호출 시")
    @Nested
    inner class TestFindAllWithUsedCount {
        @DisplayName("userId가 일치하는 결과만 조회된다")
        @Test
        fun `userId가 일치하는 결과만 조회된다`() {
            //given
            val valid = listOf(PreferenceQuest.of("t", user = user), PreferenceQuest.of("t", user = user))
            val invalid = PreferenceQuest.of("t", user = anotherUser)
            preferenceQuestRepository.saveAll(valid)
            preferenceQuestRepository.save(invalid)

            val validIds = valid.map { it.id }
            //when
            val foundIds = preferenceQuestRepository.getActiveEntitiesByUserIdWithUsedCount(user.id).map { it.id }

            //then
            assertThat(foundIds).isNotEmpty
            assertThat(foundIds).containsAll(validIds)
            assertThat(foundIds).doesNotContain(invalid.id)
        }

        @DisplayName("deletedDate가 null인 레코드만 조회된다")
        @Test
        fun `deletedDate가 null인 레코드만 조회된다`() {
            //given
            val valid = listOf(PreferenceQuest.of("t", user = user), PreferenceQuest.of("t", user = user))
            val invalid = PreferenceQuest.of("t", user = user)
            invalid.deletePreferenceQuest()
            preferenceQuestRepository.saveAll(valid)
            preferenceQuestRepository.save(invalid)

            val validIds = valid.map { it.id }
            //when
            val foundIds = preferenceQuestRepository.getActiveEntitiesByUserIdWithUsedCount(user.id).map { it.id }

            //then
            assertThat(foundIds).isNotEmpty
            assertThat(foundIds).containsAll(validIds)
            assertThat(foundIds).doesNotContain(invalid.id)
            assertThat(invalid.deletedDate).isNotNull()
        }

        @DisplayName("퀘스트 테이블에서 참조 중인 레코드 수와 같이 조회된다")
        @Test
        fun `퀘스트 테이블에서 참조 중인 레코드 수와 같이 조회된다`() {
            //given
            val pq1 = PreferenceQuest.of("t", user = user)
            val pq2 = PreferenceQuest.of("t", user = user)
            val pq3 = PreferenceQuest.of("t", user = user)
            val pqList = listOf(pq1, pq2, pq3)
            preferenceQuestRepository.saveAll(pqList)

            val usedCountList = listOf(1L, 3L, 0L)
            for (i in pqList.indices) {
                for (j in 0 until usedCountList[i]) {
                    val quest = Quest("t", user = user, seq = 1L, type = QuestType.MAIN, preferenceQuest = pqList[i])
                    questRepository.save(quest)
                }
            }

            //when
            val found = preferenceQuestRepository.getActiveEntitiesByUserIdWithUsedCount(user.id)

            //then
            assertThat(found).isNotEmpty
            for (i in pqList.indices) {
                assertThat(found[i].usedCount).isEqualTo(usedCountList[i])
            }
        }
    }



}