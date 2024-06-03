package dailyquest.user.record.service

import dailyquest.achievement.dto.SimpleAchievementAchieveRequest
import dailyquest.achievement.entity.AchievementType.*
import dailyquest.achievement.service.AchievementService
import dailyquest.user.dto.UserUpdateRequest
import dailyquest.user.record.entity.UserRecord
import dailyquest.user.record.repository.UserRecordRepository
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.repository.findByIdOrNull
import java.time.LocalDate

@ExtendWith(MockKExtension::class)
@DisplayName("유저 기록 서비스 유닛 테스트")
class UserRecordServiceUnitTest {
    @InjectMockKs
    private lateinit var userRecordService: UserRecordService
    @RelaxedMockK
    private lateinit var userRecordRepository: UserRecordRepository
    @RelaxedMockK
    private lateinit var achievementService: AchievementService
    @RelaxedMockK
    private lateinit var userRecord: UserRecord

    @BeforeEach
    fun init() {
        every { userRecordRepository.findByIdOrNull(any()) } returns userRecord
    }

    @DisplayName("골드 획득 기록 메서드 호출 시")
    @Nested
    inner class TestRecordGoldEarn {
        @DisplayName("조회한 유저의 골드 획득 기록 메서드가 호출된다")
        @Test
        fun `조회한 유저의 골드 획득 기록 메서드가 호출된다`() {
            //given
            val userId = 1L
            val userUpdateRequest = mockk<UserUpdateRequest>()
            val earnedGold = 1L
            every { userUpdateRequest.earnedGold } returns earnedGold

            //when
            userRecordService.recordGoldEarn(userId, userUpdateRequest)

            //then
            verify { userRecord.recordGoldEarn(eq(earnedGold)) }
        }

        @DisplayName("총 골드 획득 업적 달성 확인 로직이 호출된다")
        @Test
        fun `총 골드 획득 업적 달성 확인 로직이 호출된다`() {
            //given
            val userId = 1L
            val totalEarnGold = 1L
            val updateRequest = mockk<UserUpdateRequest>(relaxed = true)
            every { userRecord.goldEarnAmount } returns totalEarnGold
            val achieveRequest = SimpleAchievementAchieveRequest(GOLD_EARN, userId, totalEarnGold)

            //when
            userRecordService.recordGoldEarn(userId, updateRequest)

            //then
            verify { achievementService.checkAndAchieveAchievement(eq(achieveRequest)) }
        }
    }

    @DisplayName("퀘스트 등록 기록 메서드 호출 시")
    @Nested
    inner class TestRecordQuestRegistration {
        @DisplayName("조회한 유저의 퀘스트 등록 횟수 증가 로직이 호출된다")
        @Test
        fun `조회한 유저의 퀘스트 등록 횟수 증가 로직이 호출된다`() {
            //given
            val userId = 1L

            //when
            userRecordService.recordQuestRegistration(userId, LocalDate.now())

            //then
            verify { userRecord.increaseQuestRegistrationCount(any()) }
        }

        @DisplayName("퀘스트 등록 횟수 업적 달성 확인 로직이 호출된다")
        @Test
        fun `퀘스트 등록 횟수 업적 달성 확인 로직이 호출된다`() {
            //given
            val userId = 1L
            val questRegistrationCount = 1L
            every { userRecord.questRegistrationCount } returns questRegistrationCount
            val achieveRequest = SimpleAchievementAchieveRequest.of(QUEST_REGISTRATION, userId, questRegistrationCount)

            //when
            userRecordService.recordQuestRegistration(userId, LocalDate.now())

            //then
            verify { achievementService.checkAndAchieveAchievement(eq(achieveRequest)) }
        }

        @DisplayName("퀘스트 연속 등록 횟수 업적 달성 확인 로직이 호출된다")
        @Test
        fun `퀘스트 연속 등록 횟수 업적 달성 확인 로직이 호출된다`() {
            //given
            val userId = 1L
            val questContinuousRegistrationDays = 1L
            every { userRecord.currentQuestContinuousRegistrationDays } returns questContinuousRegistrationDays
            val achieveRequest = SimpleAchievementAchieveRequest.of(QUEST_CONTINUOUS_REGISTRATION, userId, questContinuousRegistrationDays)

            //when
            userRecordService.recordQuestRegistration(userId, LocalDate.now())

            //then
            verify { achievementService.checkAndAchieveAchievement(eq(achieveRequest)) }
        }
    }

    @DisplayName("퀘스트 완료 기록 메서드 호출 시")
    @Nested
    inner class TestRecordQuestCompletion {
        @DisplayName("조회한 유저의 퀘스트 완료 횟수 증가 로직이 호출된다")
        @Test
        fun `조회한 유저의 퀘스트 완료 횟수 증가 로직이 호출된다`() {
            //given
            val userId = 1L

            //when
            userRecordService.recordQuestCompletion(userId, LocalDate.now())

            //then
            verify { userRecord.increaseQuestCompletionCount(any()) }
        }

        @DisplayName("퀘스트 완료 횟수 업적 달성 확인 로직이 호출된다")
        @Test
        fun `퀘스트 완료 횟수 업적 달성 확인 로직이 호출된다`() {
            //given
            val userId = 1L
            val questCompletionCount = 1L
            every { userRecord.questCompletionCount } returns questCompletionCount
            val achieveRequest = SimpleAchievementAchieveRequest.of(QUEST_COMPLETION, userId, questCompletionCount)

            //when
            userRecordService.recordQuestCompletion(userId, LocalDate.now())

            //then
            verify { achievementService.checkAndAchieveAchievement(eq(achieveRequest)) }
        }

        @DisplayName("퀘스트 연속 등록 횟수 업적 달성 확인 로직이 호출된다")
        @Test
        fun `퀘스트 연속 등록 횟수 업적 달성 확인 로직이 호출된다`() {
            //given
            val userId = 1L
            val questContinuousCompletionDays = 1L
            every { userRecord.currentQuestContinuousCompletionDays } returns questContinuousCompletionDays
            val achieveRequest = SimpleAchievementAchieveRequest.of(QUEST_CONTINUOUS_COMPLETION, userId, questContinuousCompletionDays)

            //when
            userRecordService.recordQuestCompletion(userId, LocalDate.now())

            //then
            verify { achievementService.checkAndAchieveAchievement(eq(achieveRequest)) }
        }
    }
}