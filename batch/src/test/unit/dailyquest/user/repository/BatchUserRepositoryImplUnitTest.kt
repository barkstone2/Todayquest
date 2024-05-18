package dailyquest.user.repository

import com.querydsl.jpa.impl.JPAQuery
import com.querydsl.jpa.impl.JPAQueryFactory
import dailyquest.achievement.entity.Achievement
import dailyquest.achievement.entity.AchievementType.*
import dailyquest.user.entity.QUser.user
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.domain.Pageable

@ExtendWith(MockKExtension::class)
@DisplayName("배치 유저 리포지토리 구현체 유닛 테스트")
class BatchUserRepositoryImplUnitTest {
    @InjectMockKs
    private lateinit var batchUserRepository: BatchUserRepositoryImpl
    @RelaxedMockK
    private lateinit var jpaQueryFactory: JPAQueryFactory
    @RelaxedMockK
    private lateinit var userIdQuery: JPAQuery<Long>
    @RelaxedMockK
    private lateinit var countQuery: JPAQuery<Long>

    @BeforeEach
    fun beforeEach() {
        every { jpaQueryFactory.select(user.id) } returns userIdQuery
        every { userIdQuery.from(user) } returns userIdQuery
        every { userIdQuery.where(any()) } returns userIdQuery
        every { userIdQuery.offset(any()) } returns userIdQuery
        every { userIdQuery.limit(any()) } returns userIdQuery

        every { jpaQueryFactory.select(user.count()) } returns countQuery
        every { countQuery.from(any()) } returns countQuery
        every { countQuery.where(any()) } returns countQuery
        every { countQuery.fetchFirst() } returns 1
    }

    @DisplayName("업적 달성 가능 유저 ID 조회 시")
    @Nested
    inner class TestGetAllUserIdCanAchieveOf {
        private val pageable = Pageable.ofSize(1)
        @DisplayName("업적 타입 인자가 퀘스트 등록이면 퀘스트 등록 횟수가 조건으로 사용된다")
        @Test
        fun `업적 타입 인자가 퀘스트 등록이면 퀘스트 등록 횟수가 조건으로 사용된다`() {
            //given
            val achievementType = QUEST_REGISTRATION
            val targetValue: Long = 1
            val achievement = Achievement(type = achievementType, targetValue = targetValue)

            //when
            batchUserRepository.getAllUserIdWhoCanAchieveOf(achievement, pageable)

            //then
            verify { userIdQuery.where(user.questRegistrationCount.goe(targetValue)) }
        }

        @DisplayName("업적 타입 인자가 퀘스트 완료면 퀘스트 완료 횟수가 조건으로 사용된다")
        @Test
        fun `업적 타입 인자가 퀘스트 완료면 퀘스트 완료 횟수가 조건으로 사용된다`() {
            //given
            val achievementType = QUEST_COMPLETION
            val targetValue: Long = 1
            val achievement = Achievement(type = achievementType, targetValue = targetValue)

            //when
            batchUserRepository.getAllUserIdWhoCanAchieveOf(achievement, pageable)


            //then
            verify { userIdQuery.where(user.questCompletionCount.goe(targetValue)) }
        }

        @DisplayName("업적 타입 인자가 퀘스트 연속 등록이면 최대 퀘스트 연속 등록일이 조건으로 사용된다")
        @Test
        fun `업적 타입 인자가 퀘스트 연속 등록이면 최대 퀘스트 연속 등록일이 조건으로 사용된다`() {
            //given
            val achievementType = QUEST_CONTINUOUS_REGISTRATION
            val targetValue: Long = 1
            val achievement = Achievement(type = achievementType, targetValue = targetValue)

            //when
            batchUserRepository.getAllUserIdWhoCanAchieveOf(achievement, pageable)


            //then
            verify { userIdQuery.where(user.maxQuestContinuousRegistrationDays.goe(targetValue)) }
        }

        @DisplayName("업적 타입 인자가 퀘스트 연속 완료면 최대 퀘스트 연속 완료일이 조건으로 사용된다")
        @Test
        fun `업적 타입 인자가 퀘스트 연속 완료면 최대 퀘스트 연속 완료일이 조건으로 사용된다`() {
            //given
            val achievementType = QUEST_CONTINUOUS_COMPLETION
            val targetValue: Long = 1
            val achievement = Achievement(type = achievementType, targetValue = targetValue)

            //when
            batchUserRepository.getAllUserIdWhoCanAchieveOf(achievement, pageable)


            //then
            verify { userIdQuery.where(user.maxQuestContinuousCompletionDays.goe(targetValue)) }
        }

        @DisplayName("업적 타입 인자가 골드 획득이면 골드 총 획득량이 조건으로 사용된다")
        @Test
        fun `업적 타입 인자가 골드 획득이면 골드 총 획득량이 조건으로 사용된다`() {
            //given
            val achievementType = GOLD_EARN
            val targetValue: Long = 1
            val achievement = Achievement(type = achievementType, targetValue = targetValue)

            //when
            batchUserRepository.getAllUserIdWhoCanAchieveOf(achievement, pageable)


            //then
            verify { userIdQuery.where(user.goldEarnAmount.goe(targetValue)) }
        }

        @DisplayName("업적 타입 인자가 완벽한 하루면 완벽한 하루 횟수가 조건으로 사용된다")
        @Test
        fun `업적 타입 인자가 완벽한 하루면 완벽한 하루 횟수가 조건으로 사용된다`() {
            //given
            val achievementType = PERFECT_DAY
            val targetValue: Long = 1
            val achievement = Achievement(type = achievementType, targetValue = targetValue)

            //when
            batchUserRepository.getAllUserIdWhoCanAchieveOf(achievement, pageable)


            //then
            verify { userIdQuery.where(user.perfectDayCount.goe(targetValue)) }
        }
    }
}