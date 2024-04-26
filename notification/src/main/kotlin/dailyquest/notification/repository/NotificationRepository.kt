package dailyquest.notification.repository

import dailyquest.notification.entity.Notification
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface NotificationRepository: JpaRepository<Notification, Long>, NotificationRepositoryCustom {
    fun getNotificationByIdAndUserId(notificationId: Long, userId: Long): Notification?
    fun countByUserIdAndConfirmedDateIsNull(userId: Long): Int

    @Modifying(clearAutomatically = true)
    @Query("update Notification n set n.confirmedDate = now() where n.userId = :userId and n.confirmedDate is null and n.deletedDate is null")
    fun confirmAllNotifications(@Param("userId") userId: Long)

    @Modifying(clearAutomatically = true)
    @Query("update Notification n set n.deletedDate = now() where n.userId = :userId and n.deletedDate is null")
    fun deleteAllNotifications(@Param("userId") userId: Long)
}