package dailyquest.user.record.repository

import com.querydsl.jpa.impl.JPAQueryFactory
import dailyquest.achievement.entity.Achievement
import dailyquest.achievement.entity.AchievementType.*
import dailyquest.user.record.entity.QUserRecord.userRecord
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable

class BatchUserRecordRepositoryImpl(
    private val jpaQueryFactory: JPAQueryFactory
): BatchUserRecordRepositoryCustom {

    override fun getAllUserIdWhoCanAchieveOf(
        targetAchievement: Achievement,
        pageable: Pageable
    ): Page<Long> {
        val recordForType = when (targetAchievement.type) {
            QUEST_REGISTRATION -> userRecord.questRegistrationCount
            QUEST_COMPLETION -> userRecord.questCompletionCount
            QUEST_CONTINUOUS_REGISTRATION -> userRecord.maxQuestContinuousRegistrationDays
            QUEST_CONTINUOUS_COMPLETION -> userRecord.maxQuestContinuousCompletionDays
            GOLD_EARN -> userRecord.goldEarnAmount
            PERFECT_DAY -> userRecord.perfectDayCount
        }

        val isRecordForTypeGoeThanTargetValue = recordForType.goe(targetAchievement.targetValue)
        val users = jpaQueryFactory.select(userRecord.id)
            .from(userRecord)
            .where(isRecordForTypeGoeThanTargetValue)
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .fetch()

        val totalCount = jpaQueryFactory.select(userRecord.count())
            .from(userRecord)
            .where(isRecordForTypeGoeThanTargetValue)
            .fetchFirst()
        return PageImpl(users, pageable, totalCount)
    }
}