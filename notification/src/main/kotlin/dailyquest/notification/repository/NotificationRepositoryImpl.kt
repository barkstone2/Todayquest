package dailyquest.notification.repository

import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.jpa.impl.JPAQueryFactory
import dailyquest.notification.dto.NotificationCondition
import dailyquest.notification.entity.Notification
import dailyquest.notification.entity.QNotification.notification
import jakarta.annotation.PostConstruct
import jakarta.persistence.EntityManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable

class NotificationRepositoryImpl @Autowired constructor(
    private val entityManager: EntityManager,
): NotificationRepositoryCustom {
    private lateinit var query: JPAQueryFactory

    @PostConstruct
    private fun init() {
        query = JPAQueryFactory(entityManager)
    }

    override fun getNotConfirmedNotifications(
        userId: Long,
        condition: NotificationCondition,
        pageable: Pageable
    ): Page<Notification> {
        val notConfirmed = notification.confirmedDate.isNull
        val whereExpression = hasSameUserId(userId).and(notDeleted()).and(hasSameType(condition)).and(notConfirmed)
        val notifications = this.getPagedNotificationsBasedOnCondition(pageable, whereExpression)
        return notifications
    }

    override fun getActiveNotifications(
        userId: Long,
        condition: NotificationCondition,
        pageable: Pageable
    ): Page<Notification> {
        val whereExpression = hasSameUserId(userId).and(notDeleted()).and(hasSameType(condition))
        val notifications = this.getPagedNotificationsBasedOnCondition(pageable, whereExpression)
        return notifications
    }

    private fun hasSameUserId(userId: Long): BooleanExpression {
        return notification.userId.eq(userId)
    }

    private fun notDeleted(): BooleanExpression {
        return notification.deletedDate.isNull
    }

    private fun hasSameType(condition: NotificationCondition): BooleanExpression? {
        return if (condition.type != null) notification.type.eq(condition.type) else null
    }

    private fun getPagedNotificationsBasedOnCondition(pageable: Pageable, whereExpression: BooleanExpression): Page<Notification> {
        val notifications = this.getNotificationsBasedOnCondition(pageable, whereExpression)
        val totalCount = this.getTotalCountBasedOnCondition(whereExpression)
        return PageImpl(notifications, pageable, totalCount)
    }

    private fun getNotificationsBasedOnCondition(pageable: Pageable, whereExpressions: BooleanExpression): List<Notification> {
        val createdDateDesc = notification.createdDate.desc()
        val notifications = query
            .select(notification)
            .from(notification)
            .where(whereExpressions)
            .orderBy(createdDateDesc)
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .fetch()
        return notifications
    }

    private fun getTotalCountBasedOnCondition(whereExpressions: BooleanExpression): Long {
        val totalCount = query
            .select(notification.count().longValue())
            .from(notification)
            .where(whereExpressions)
            .fetchOne()
        return totalCount!!
    }
}