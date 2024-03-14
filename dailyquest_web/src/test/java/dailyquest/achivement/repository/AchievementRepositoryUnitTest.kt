package dailyquest.achivement.repository

import dailyquest.achievement.entity.Achievement
import dailyquest.achievement.entity.AchievementAchieveLog
import dailyquest.achievement.entity.AchievementType
import dailyquest.achievement.entity.AchievementType.*
import dailyquest.achievement.repository.AchievementAchieveLogRepository
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

@ExtendWith(MockKExtension::class)
@DisplayName("업적 리포지토리 유닛 테스트")
@DataJpaTest
class AchievementRepositoryUnitTest {

    @Autowired
    private lateinit var achievementRepository: AchievementRepository

    @Autowired
    private lateinit var achieveLogRepository: AchievementAchieveLogRepository

    @Autowired
    private lateinit var userRepository: UserRepository

    @DisplayName("findNotAchievedAchievement 호출 시")
    @Nested
    inner class TestFindNotAchievedAchievement {
        @DisplayName("등록된 업적이 없으면 null이 반환된다")
        @Test
        fun `등록된 업적이 없으면 null이 반환된다`() {
            //when
            val result = achievementRepository.findNotAchievedAchievement(QUEST_REGISTRATION, 1L)

            //then
            assertThat(result).isNull()
        }

        @DisplayName("인자로 넘어온 타입의 업적이 조회된다")
        @Test
        fun `인자로 넘어온 타입의 업적이 조회된다`() {
            //given
            val achievementType = QUEST_REGISTRATION
            achievementRepository.save(Achievement("", "", achievementType, 1))
            achievementRepository.save(Achievement("", "", QUEST_COMPLETION, 1))

            //when
            val result = achievementRepository.findNotAchievedAchievement(achievementType, 1L)

            //then
            assertThat(result?.type).isEqualTo(achievementType)
        }

        @DisplayName("다른 타입 업적의 목표 횟수가 더 작아도 인자로 넘어온 타입 업적이 조회된다")
        @Test
        fun `다른 타입 업적의 목표 횟수가 더 작아도 인자로 넘어온 타입 업적이 조회된다`() {
            //given
            val achievementType = QUEST_REGISTRATION
            achievementRepository.save(Achievement("", "", achievementType, 10))
            achievementRepository.save(Achievement("", "", QUEST_COMPLETION, 1))

            //when
            val result = achievementRepository.findNotAchievedAchievement(achievementType, 1L)

            //then
            assertThat(result?.type).isEqualTo(achievementType)
        }

        @DisplayName("인자로 넘어온 유저가 달성하지 않은 업적이 조회된다")
        @Test
        fun `인자로 넘어온 유저가 달성하지 않은 업적이 조회된다`() {
            //given
            val type = QUEST_REGISTRATION
            val userId = 1L
            val achievedAchievement = Achievement("", "", type, 1)
            val notAchievedAchievement = Achievement("", "", type, 1)
            achievementRepository.save(achievedAchievement)
            achievementRepository.save(notAchievedAchievement)
            val userReference = userRepository.getReferenceById(userId)
            achieveLogRepository.save(AchievementAchieveLog.of(achievedAchievement, userReference))

            //when
            val result = achievementRepository.findNotAchievedAchievement(type, userId)

            //then
            assertThat(result).isEqualTo(notAchievedAchievement)
        }

        @DisplayName("다른 유저가 달성한 업적이라도 현재 유저가 달성하지 않았다면 조회된다")
        @Test
        fun `다른 유저가 달성한 업적이라도 현재 유저가 달성하지 않았다면 조회된다`() {
            //given
            val type = QUEST_REGISTRATION
            val userId = 1L
            val achievedAchievementByOther = Achievement("", "", type, 1)
            achievementRepository.save(achievedAchievementByOther)
            val userReference = userRepository.getReferenceById(2L)
            achieveLogRepository.save(AchievementAchieveLog.of(achievedAchievementByOther, userReference))

            //when
            val result = achievementRepository.findNotAchievedAchievement(type, userId)

            //then
            assertThat(result).isEqualTo(achievedAchievementByOther)
        }

        @DisplayName("달성한 업적의 목표 횟수가 더 작아도 달성하지 않은 업적이 조회된다")
        @Test
        fun `달성한 업적의 목표 횟수가 더 작아도 달성하지 않은 업적이 조회된다`() {
            //given
            val type = QUEST_REGISTRATION
            val userId = 1L
            val achievedAchievement = Achievement("", "", type, 1)
            val notAchievedAchievement = Achievement("", "", type, 10)
            achievementRepository.save(achievedAchievement)
            achievementRepository.save(notAchievedAchievement)
            val userReference = userRepository.getReferenceById(userId)
            achieveLogRepository.save(AchievementAchieveLog.of(achievedAchievement, userReference))

            //when
            val result = achievementRepository.findNotAchievedAchievement(type, userId)

            //then
            assertThat(result).isEqualTo(notAchievedAchievement)
        }

        @DisplayName("달성한 업적의 목표 횟수가 더 커도 달성하지 않은 업적이 조회된다")
        @Test
        fun `달성한 업적의 목표 횟수가 더 커도 달성하지 않은 업적이 조회된다`() {
            //given
            val type = QUEST_REGISTRATION
            val userId = 1L
            val achievedAchievement = Achievement("", "", type, 10)
            val notAchievedAchievement = Achievement("", "", type, 1)
            achievementRepository.save(achievedAchievement)
            achievementRepository.save(notAchievedAchievement)
            val userReference = userRepository.getReferenceById(userId)
            achieveLogRepository.save(AchievementAchieveLog.of(achievedAchievement, userReference))

            //when
            val result = achievementRepository.findNotAchievedAchievement(type, userId)

            //then
            assertThat(result).isEqualTo(notAchievedAchievement)
        }

        @DisplayName("달성한 업적 뿐이면 null이 반환된다")
        @Test
        fun `달성한 업적 뿐이면 null이 반환된다`() {
            //given
            val type = QUEST_REGISTRATION
            val userId = 1L
            val achievedAchievement = Achievement("", "", type, 1)
            achievementRepository.save(achievedAchievement)
            val userReference = userRepository.getReferenceById(userId)
            achieveLogRepository.save(AchievementAchieveLog.of(achievedAchievement, userReference))

            //when
            val result = achievementRepository.findNotAchievedAchievement(type, userId)

            //then
            assertThat(result).isNull()
        }

        @DisplayName("달성하지 않은 업적 중 목표 횟수가 가장 적은 업적이 조회된다")
        @Test
        fun `달성하지 않은 업적 중 목표 횟수가 가장 적은 업적이 조회된다`() {
            //given
            val type = QUEST_REGISTRATION
            val userId = 1L
            val smallestTargetAchievement = Achievement("", "", type, 1)
            val otherAchievement = Achievement("", "", type, 2)
            achievementRepository.save(smallestTargetAchievement)
            achievementRepository.save(otherAchievement)

            //when
            val result = achievementRepository.findNotAchievedAchievement(type, userId)

            //then
            assertThat(result).isEqualTo(smallestTargetAchievement)
        }
    }

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
            val achievementType = QUEST_REGISTRATION

            //when
            val achievements = achievementRepository.getAchievementsWithAchieveInfo(achievementType, user.id)

            //then
            assertThat(achievements).isNotEmpty.allMatch { it.type == achievementType }
        }

        @DisplayName("완료한 업적은 완료 상태로 표시되고 달성 날짜가 포함된다")
        @Test
        fun `완료한 업적은 완료 상태로 표시되고 달성 날짜가 포함된다`() {
            //given
            val achievementType = QUEST_REGISTRATION
            achieveLogRepository.save(AchievementAchieveLog(achievementMapByType[achievementType]!!, user))

            //when
            val achievements = achievementRepository.getAchievementsWithAchieveInfo(achievementType, user.id)

            //then
            assertThat(achievements).isNotEmpty.allMatch { it.isAchieved && it.achievedDate != null }
        }

        @DisplayName("달성하지 않은 업적은 달성 여부가 false이고 달성 날짜가 null이다")
        @Test
        fun `달성하지 않은 업적은 달성 여부가 false이고 달성 날짜가 null이다`() {
            //given
            //when
            val achievements = achievementRepository.getAchievementsWithAchieveInfo(QUEST_REGISTRATION, user.id)

            //then
            assertThat(achievements).isNotEmpty.allMatch { !it.isAchieved && it.achievedDate == null }
        }

        @DisplayName("다른 유저가 달성한 업적이라도 현재 유저가 달성하지 않았다면 미달성으로 표시된다")
        @Test
        fun `다른 유저가 달성한 업적이라도 현재 유저가 달성하지 않았다면 미달성으로 표시된다`() {
            //given
            val achievementType = QUEST_REGISTRATION
            achieveLogRepository.save(AchievementAchieveLog(achievementMapByType[achievementType]!!, user))

            //when
            val achievements = achievementRepository.getAchievementsWithAchieveInfo(achievementType, user.id+1)

            //then
            assertThat(achievements).isNotEmpty.allMatch { !it.isAchieved && it.achievedDate == null }
        }
    }
}