package dailyquest.achivement.repository

import dailyquest.achievement.entity.Achievement
import dailyquest.achievement.entity.AchievementLog
import dailyquest.achievement.entity.AchievementType
import dailyquest.achievement.repository.AchievementLogRepository
import dailyquest.achievement.repository.AchievementRepository
import dailyquest.user.entity.ProviderType
import dailyquest.user.entity.UserInfo
import dailyquest.user.repository.UserRepository
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import kotlin.random.Random

@ExtendWith(MockKExtension::class)
@DisplayName("업적 리포지토리 유닛 테스트")
@DataJpaTest
class AchievementRepositoryUnitTest {

    @Autowired
    private lateinit var achievementRepository: AchievementRepository

    @Autowired
    private lateinit var achievementLogRepository: AchievementLogRepository

    @Autowired
    private lateinit var userRepository: UserRepository

    @DisplayName("getAchievementsWithAchieveInfo 호출 시")
    @Nested
    inner class TestGetAchievementsWithAchieveInfo {
        private lateinit var user: UserInfo
        private lateinit var achievementMapByType: MutableMap<AchievementType, Achievement>

        @BeforeEach
        fun init() {
            achievementMapByType = mutableMapOf()
            AchievementType.values().forEach {
                val achievement = Achievement(it.name, "${it.name} type achievement", it, 1)
                achievementRepository.save(achievement)
                achievementMapByType[it] = achievement
            }
            user = userRepository.save(UserInfo("user", "nick", ProviderType.GOOGLE))
        }

        @DisplayName("요청 타입에 해당하는 업적만 조회된다")
        @Test
        fun `요청 타입에 해당하는 업적만 조회된다`() {
            //given
            val achievementType = AchievementType.QUEST_REGISTRATION

            //when
            val achievements = achievementRepository.getAchievementsWithAchieveInfo(achievementType, user.id)

            //then
            assertThat(achievements).isNotEmpty.allMatch { it.type == achievementType }
        }

        @DisplayName("완료한 업적은 완료 상태로 표시된다")
        @Test
        fun `완료한 업적은 완료 상태로 표시된다`() {
            //given
            val achievementType = AchievementType.QUEST_REGISTRATION
            achievementLogRepository.save(AchievementLog(achievementMapByType[achievementType]!!, user))

            //when
            val achievements = achievementRepository.getAchievementsWithAchieveInfo(achievementType, user.id)

            //then
            assertThat(achievements).isNotEmpty.allMatch { it.isAchieved }
        }
    }
}