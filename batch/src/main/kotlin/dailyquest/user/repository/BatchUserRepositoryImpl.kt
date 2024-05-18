package dailyquest.user.repository

import com.querydsl.jpa.impl.JPAQueryFactory
import dailyquest.achievement.entity.Achievement
import dailyquest.achievement.entity.AchievementType.*
import dailyquest.user.entity.QUser.user
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable

class BatchUserRepositoryImpl(
    private val jpaQueryFactory: JPAQueryFactory
): BatchUserRepositoryCustom {

    override fun getAllUserIdWhoCanAchieveOf(
        targetAchievement: Achievement,
        pageable: Pageable
    ): Page<Long> {
        val recordForType = when (targetAchievement.type) {
            QUEST_REGISTRATION -> user.questRegistrationCount
            QUEST_COMPLETION -> user.questCompletionCount
            QUEST_CONTINUOUS_REGISTRATION -> user.maxQuestContinuousRegistrationDays
            QUEST_CONTINUOUS_COMPLETION -> user.maxQuestContinuousCompletionDays
            GOLD_EARN -> user.goldEarnAmount
            PERFECT_DAY -> user.perfectDayCount
        }

        val isRecordForTypeGoeThanTargetValue = recordForType.goe(targetAchievement.targetValue)
        val users = jpaQueryFactory.select(user.id)
            .from(user)
            .where(isRecordForTypeGoeThanTargetValue)
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .fetch()

        val totalCount = jpaQueryFactory.select(user.count())
            .from(user)
            .where(isRecordForTypeGoeThanTargetValue)
            .fetchFirst()
        return PageImpl(users, pageable, totalCount)
    }
}