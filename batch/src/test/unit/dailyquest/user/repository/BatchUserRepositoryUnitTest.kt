package dailyquest.user.repository

import dailyquest.user.entity.ProviderType
import dailyquest.user.entity.User
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.data.domain.Pageable

@DataJpaTest
class BatchUserRepositoryUnitTest {
    @Autowired
    private lateinit var batchUserRepository: BatchUserRepository

    @DisplayName("findAllByQuestRegistrationCountGreaterThanEqual 호출 시")
    @Nested
    inner class TestFindQuestRegCountMethod {
        @DisplayName("퀘스트 등록 수가 인자 이상인 유저만 조회된다")
        @Test
        fun `퀘스트 등록 수가 인자 이상인 유저만 조회된다`() {
            //given
            batchUserRepository.save(User("1", "1", ProviderType.GOOGLE, questRegistrationCount = 0))
            batchUserRepository.save(User("1", "2", ProviderType.GOOGLE, questCompletionCount = 1))
            val shouldFindUser = batchUserRepository.save(User("2", "3", ProviderType.GOOGLE, questRegistrationCount = 1))

            //when
            val result =
                batchUserRepository.findAllByQuestRegistrationCountGreaterThanEqual(1, Pageable.unpaged())

            //then
            assertThat(result).containsExactly(shouldFindUser)
        }
    }
}