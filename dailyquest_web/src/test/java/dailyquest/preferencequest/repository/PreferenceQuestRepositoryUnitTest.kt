package dailyquest.preferencequest.repository

import dailyquest.config.JpaAuditingConfiguration
import dailyquest.preferencequest.entity.PreferenceQuest
import dailyquest.quest.entity.Quest
import dailyquest.quest.entity.QuestType
import dailyquest.quest.repository.QuestRepository
import dailyquest.user.entity.ProviderType
import dailyquest.user.entity.UserInfo
import dailyquest.user.repository.UserRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import java.time.LocalDateTime

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

    var userInfo: UserInfo = UserInfo("", "", ProviderType.GOOGLE)
    var anotherUser: UserInfo = UserInfo("", "", ProviderType.GOOGLE)

    @BeforeEach
    fun init() {
        userInfo = if(userInfo.id == 0L) userRepository.save(UserInfo("", "user1", ProviderType.GOOGLE)) else userInfo
        anotherUser = if(anotherUser.id == 0L) userRepository.save(UserInfo("", "user2", ProviderType.GOOGLE)) else anotherUser
        anotherUser.updateCoreTime(0, LocalDateTime.now())
        userRepository.saveAndFlush(anotherUser)
    }

    @DisplayName("findAllWithUsedCount 호출 시")
    @Nested
    inner class TestFindAllWithUsedCount {
        @DisplayName("userId가 일치하는 결과만 조회된다")
        @Test
        fun `userId가 일치하는 결과만 조회된다`() {
            //given
            val valid = listOf(PreferenceQuest("t", user = userInfo), PreferenceQuest("t", user = userInfo))
            val invalid = PreferenceQuest("t", user = anotherUser)
            preferenceQuestRepository.saveAll(valid)
            preferenceQuestRepository.save(invalid)

            val validIds = valid.map { it.id }
            //when
            val foundIds = preferenceQuestRepository.findAllWithUsedCount(userInfo.id).map { it.id }

            //then
            assertThat(foundIds).isNotEmpty
            assertThat(foundIds).containsAll(validIds)
            assertThat(foundIds).doesNotContain(invalid.id)
        }

        @DisplayName("deletedDate가 null인 레코드만 조회된다")
        @Test
        fun `deletedDate가 null인 레코드만 조회된다`() {
            //given
            val valid = listOf(PreferenceQuest("t", user = userInfo), PreferenceQuest("t", user = userInfo))
            val invalid = PreferenceQuest("t", user = userInfo)
            invalid.deletePreferenceQuest()
            preferenceQuestRepository.saveAll(valid)
            preferenceQuestRepository.save(invalid)

            val validIds = valid.map { it.id }
            //when
            val foundIds = preferenceQuestRepository.findAllWithUsedCount(userInfo.id).map { it.id }

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
            val pq1 = PreferenceQuest("t", user = userInfo)
            val pq2 = PreferenceQuest("t", user = userInfo)
            val pq3 = PreferenceQuest("t", user = userInfo)
            val pqList = listOf(pq1, pq2, pq3)
            preferenceQuestRepository.saveAll(pqList)

            val usedCountList = listOf(1L, 3L, 0L)
            for (i in pqList.indices) {
                for (j in 0 until usedCountList[i]) {
                    val quest = Quest("t", user = userInfo, seq = 1L, type = QuestType.MAIN, preferenceQuest = pqList[i])
                    questRepository.save(quest)
                }
            }

            //when
            val found = preferenceQuestRepository.findAllWithUsedCount(userInfo.id)

            //then
            assertThat(found).isNotEmpty
            for (i in pqList.indices) {
                assertThat(found[i].usedCount).isEqualTo(usedCountList[i])
            }
        }
    }



}