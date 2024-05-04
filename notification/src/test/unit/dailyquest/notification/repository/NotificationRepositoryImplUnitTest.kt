package dailyquest.notification.repository

import dailyquest.notification.dto.NotificationCondition
import dailyquest.notification.entity.Notification
import dailyquest.notification.entity.NotificationType
import jakarta.persistence.EntityManager
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.data.domain.PageRequest
import java.time.LocalDateTime

@DisplayName("알림 리포지토리 구현체 유닛 테스트")
@DataJpaTest
class NotificationRepositoryImplUnitTest {
    @Autowired
    private lateinit var notificationRepository: NotificationRepository

    @Autowired
    private lateinit var entityManager: EntityManager

    @DisplayName("getNotConfirmedNotifications 호출 시")
    @Nested
    inner class TestGetNotConfirmedNotifications {
        private val userId = 1L
        private val otherUserId = 2L

        @DisplayName("요청한 pageSize 개수만큼의 데이터가 반환된다")
        @Test
        fun `요청한 pageSize 개수만큼의 데이터가 반환된다`() {
            //given
            val condition = NotificationCondition()
            val pageSize = 2
            val pageRequest = PageRequest.of(0, pageSize)

            //when
            val result =
                notificationRepository.getNotConfirmedNotifications(userId, condition, pageRequest)

            //then
            assertThat(result.size).isEqualTo(pageSize)
        }
        
        @DisplayName("요청한 page의 데이터가 반환된다")
        @Test
        fun `요청한 page의 데이터가 반환된다`() {
            //given
            val condition = NotificationCondition()
            val page = 1
            val pageRequest = PageRequest.of(page, 2)

            //when
            val result =
                notificationRepository.getNotConfirmedNotifications(userId, condition, pageRequest)

            //then
            assertThat(result.number).isEqualTo(page)
        }
        
        @DisplayName("생성일 내림차순으로 정렬된 데이터가 반환된다")
        @Test
        fun `생성일 내림차순으로 정렬된 데이터가 반환된다`() {
            //given
            repeat(3) {
                notificationRepository.save(Notification.of(NotificationType.ACHIEVEMENT_ACHIEVE, userId, ""))
            }
            val condition = NotificationCondition()
            val pageRequest = PageRequest.of(0, 3)

            //when
            val result =
                notificationRepository.getNotConfirmedNotifications(userId, condition, pageRequest)

            //then
            assertThat(result.content).isSortedAccordingTo(Comparator.comparing(Notification::createdDate).reversed())
        }

        @DisplayName("confirmedDate가 null이 아닌 데이터는 조회되지 않는다")
        @Test
        fun `confirmedDate가 null이 아닌 데이터는 조회되지 않는다`() {
            //given
            val shouldNotContainIds = listOf(1L, 2L)
            val confirmedDate = LocalDateTime.of(2000, 12, 12, 12, 0)
            val query =
                entityManager.createNativeQuery("insert into notification (notification_id, user_id, title, content, type, metadata, created_date, confirmed_date, deleted_date) VALUES (?, ?, 't', 'c', 'ACHIEVEMENT_ACHIEVE', '', now(), ?, null)")
            query.setParameter(2, userId)
            shouldNotContainIds.forEach {
                query.setParameter(1, it)
                query.setParameter(3, confirmedDate)
                query.executeUpdate()
            }
            val condition = NotificationCondition()
            val pageRequest = PageRequest.of(0, 3)

            //when
            val result =
                notificationRepository.getNotConfirmedNotifications(userId, condition, pageRequest)

            //then
            assertThat(result.content).noneMatch { shouldNotContainIds.contains(it.id) }
        }
        
        @DisplayName("userId가 다른 데이터는 조회되지 않는다")
        @Test
        fun `userId가 다른 데이터는 조회되지 않는다`() {
            //given
            notificationRepository.save(Notification.of(NotificationType.ACHIEVEMENT_ACHIEVE, userId, ""))
            val shouldNotContain = Notification.of(NotificationType.ACHIEVEMENT_ACHIEVE, otherUserId, "")
            notificationRepository.save(shouldNotContain)

            val condition = NotificationCondition()
            val pageRequest = PageRequest.of(0, 3)

            //when
            val result =
                notificationRepository.getNotConfirmedNotifications(userId, condition, pageRequest)

            //then
            assertThat(result.content).doesNotContain(shouldNotContain)
        }
        
        @DisplayName("deletedDate가 null이 아닌 데이터는 조회되지 않는다")
        @Test
        fun `deletedDate가 null이 아닌 데이터는 조회되지 않는다`() {
            //given
            val shouldNotContainIds = listOf(1L, 2L)
            val deletedDate = LocalDateTime.of(2000, 12, 12, 12, 0)
            val query =
                entityManager.createNativeQuery("insert into notification (notification_id, user_id, title, content, type, metadata, created_date, confirmed_date, deleted_date) VALUES (?, ?, 't', 'c', 'ACHIEVEMENT_ACHIEVE', '', now(), null, ?)")
            query.setParameter(2, userId)
            shouldNotContainIds.forEach {
                query.setParameter(1, it)
                query.setParameter(3, deletedDate)
                query.executeUpdate()
            }
            val condition = NotificationCondition()
            val pageRequest = PageRequest.of(0, 3)

            //when
            val result =
                notificationRepository.getNotConfirmedNotifications(userId, condition, pageRequest)

            //then
            assertThat(result.content).noneMatch { shouldNotContainIds.contains(it.id) }
        }
        
        @DisplayName("요청 타입 조건이 없으면 모든 타입의 데이터가 조회된다")
        @Test
        fun `요청 타입 조건이 없으면 모든 타입의 데이터가 조회된다`() {
            //given
            // TODO 알림 타입이 추가되면 테스트 수정
            val shouldContains = listOf(
                notificationRepository.save(Notification.of(NotificationType.ACHIEVEMENT_ACHIEVE, userId, ""))
            )
            val condition = NotificationCondition()
            val pageRequest = PageRequest.of(0, 100)

            //when
            val result =
                notificationRepository.getNotConfirmedNotifications(userId, condition, pageRequest)

            //then
            assertThat(result.content).containsAll(shouldContains)
        }
        
        @DisplayName("요청 타입 조건이 있으면 요청한 타입의 데이터만 조회된다")
        @Test
        fun `요청 타입 조건이 있으면 요청한 타입의 데이터만 조회된다`() {
            //given
            // TODO 알림 타입이 추가되면 테스트 수정
            val shouldContains = listOf(
                notificationRepository.save(Notification.of(NotificationType.ACHIEVEMENT_ACHIEVE, userId, ""))
            )
            val condition = NotificationCondition()
            val pageRequest = PageRequest.of(0, 100)

            //when
            val result =
                notificationRepository.getNotConfirmedNotifications(userId, condition, pageRequest)

            //then
            assertThat(result.content).containsAll(shouldContains)
        }
        
        @DisplayName("반환되는 데이터의 총 개수가 실제 조회 대상 데이터의 수와 일치한다")
        @Test
        fun `반환되는 데이터의 총 개수가 실제 조회 대상 데이터의 수와 일치한다`() {
            //given
            val shouldContains = listOf(
                notificationRepository.save(Notification.of(NotificationType.ACHIEVEMENT_ACHIEVE, userId, "")),
                notificationRepository.save(Notification.of(NotificationType.ACHIEVEMENT_ACHIEVE, userId, "")),
            )
            notificationRepository.save(Notification.of(NotificationType.ACHIEVEMENT_ACHIEVE, otherUserId, ""))
            val insertQueryHasConfirmedDate =
                "insert into notification (user_id, title, content, type, metadata, created_date, confirmed_date, deleted_date) VALUES (?, 't', 'c', 'ACHIEVEMENT_ACHIEVE', '', now(), now(), null)"
            val insertQueryHasDeletedDate =
                "insert into notification (user_id, title, content, type, metadata, created_date, confirmed_date, deleted_date) VALUES (?, 't', 'c', 'ACHIEVEMENT_ACHIEVE', '', now(), null, now())"
            with(entityManager) {
                with(createNativeQuery(insertQueryHasConfirmedDate)) {
                    setParameter(1, userId)
                    executeUpdate()
                }
                with(createNativeQuery(insertQueryHasDeletedDate)) {
                    setParameter(1, userId)
                    executeUpdate()
                }
            }

            val condition = NotificationCondition()
            val pageRequest = PageRequest.of(0, 100)

            //when
            val result =
                notificationRepository.getNotConfirmedNotifications(userId, condition, pageRequest)

            //then
            assertThat(result.totalElements).isEqualTo(shouldContains.size.toLong())
        }
    }

    @DisplayName("getActiveNotifications 호출 시")
    @Nested
    inner class TestGetActiveNotifications {
        private val userId = 1L
        private val otherUserId = 2L

        @DisplayName("요청한 pageSize 개수만큼의 데이터가 반환된다")
        @Test
        fun `요청한 pageSize 개수만큼의 데이터가 반환된다`() {
            //given
            val condition = NotificationCondition()
            val pageSize = 2
            val pageRequest = PageRequest.of(0, pageSize)

            //when
            val result =
                notificationRepository.getActiveNotifications(userId, condition, pageRequest)

            //then
            assertThat(result.size).isEqualTo(pageSize)
        }

        @DisplayName("요청한 page의 데이터가 반환된다")
        @Test
        fun `요청한 page의 데이터가 반환된다`() {
            //given
            val condition = NotificationCondition()
            val page = 1
            val pageRequest = PageRequest.of(page, 2)

            //when
            val result =
                notificationRepository.getActiveNotifications(userId, condition, pageRequest)

            //then
            assertThat(result.number).isEqualTo(page)
        }

        @DisplayName("생성일 내림차순으로 정렬된 데이터가 반환된다")
        @Test
        fun `생성일 내림차순으로 정렬된 데이터가 반환된다`() {
            //given
            repeat(3) {
                notificationRepository.save(Notification.of(NotificationType.ACHIEVEMENT_ACHIEVE, userId, ""))
            }
            val condition = NotificationCondition()
            val pageRequest = PageRequest.of(0, 3)

            //when
            val result =
                notificationRepository.getActiveNotifications(userId, condition, pageRequest)

            //then
            assertThat(result.content).isSortedAccordingTo(Comparator.comparing(Notification::createdDate).reversed())
        }

        @DisplayName("confirmedDate가 null이 아닌 데이터도 조회된다")
        @Test
        fun `confirmedDate가 null이 아닌 데이터도 조회된다`() {
            //given
            val shouldContainIds = listOf(1L, 2L)
            val confirmedDate = LocalDateTime.of(2000, 12, 12, 12, 0)
            val query =
                entityManager.createNativeQuery("insert into notification (notification_id, user_id, title, content, type, metadata, created_date, confirmed_date, deleted_date) VALUES (?, ?, 't', 'c', 'ACHIEVEMENT_ACHIEVE', '', now(), ?, null)")
            query.setParameter(2, userId)
            shouldContainIds.forEach {
                query.setParameter(1, it)
                query.setParameter(3, confirmedDate)
                query.executeUpdate()
            }
            val condition = NotificationCondition()
            val pageRequest = PageRequest.of(0, 3)

            //when
            val result =
                notificationRepository.getActiveNotifications(userId, condition, pageRequest)

            //then
            assertThat(result.content).allMatch { shouldContainIds.contains(it.id) }
        }

        @DisplayName("userId가 다른 데이터는 조회되지 않는다")
        @Test
        fun `userId가 다른 데이터는 조회되지 않는다`() {
            //given
            notificationRepository.save(Notification.of(NotificationType.ACHIEVEMENT_ACHIEVE, userId, ""))
            val shouldNotContain = Notification.of(NotificationType.ACHIEVEMENT_ACHIEVE, otherUserId, "")
            notificationRepository.save(shouldNotContain)

            val condition = NotificationCondition()
            val pageRequest = PageRequest.of(0, 3)

            //when
            val result =
                notificationRepository.getActiveNotifications(userId, condition, pageRequest)

            //then
            assertThat(result.content).doesNotContain(shouldNotContain)
        }

        @DisplayName("deletedDate가 null이 아닌 데이터는 조회되지 않는다")
        @Test
        fun `deletedDate가 null이 아닌 데이터는 조회되지 않는다`() {
            //given
            val shouldNotContainIds = listOf(1L, 2L)
            val deletedDate = LocalDateTime.of(2000, 12, 12, 12, 0)
            val query =
                entityManager.createNativeQuery("insert into notification (notification_id, user_id, title, content, type, metadata, created_date, confirmed_date, deleted_date) VALUES (?, ?, 't', 'c', 'ACHIEVEMENT_ACHIEVE', '', now(), null, ?)")
            query.setParameter(2, userId)
            shouldNotContainIds.forEach {
                query.setParameter(1, it)
                query.setParameter(3, deletedDate)
                query.executeUpdate()
            }
            val condition = NotificationCondition()
            val pageRequest = PageRequest.of(0, 3)

            //when
            val result =
                notificationRepository.getActiveNotifications(userId, condition, pageRequest)

            //then
            assertThat(result.content).noneMatch { shouldNotContainIds.contains(it.id) }
        }

        @DisplayName("요청 타입 조건이 없으면 모든 타입의 데이터가 조회된다")
        @Test
        fun `요청 타입 조건이 없으면 모든 타입의 데이터가 조회된다`() {
            //given
            // TODO 알림 타입이 추가되면 테스트 수정
            val shouldContains = listOf(
                notificationRepository.save(Notification.of(NotificationType.ACHIEVEMENT_ACHIEVE, userId, ""))
            )
            val condition = NotificationCondition()
            val pageRequest = PageRequest.of(0, 100)

            //when
            val result =
                notificationRepository.getActiveNotifications(userId, condition, pageRequest)

            //then
            assertThat(result.content).containsAll(shouldContains)
        }

        @DisplayName("요청 타입 조건이 있으면 요청한 타입의 데이터만 조회된다")
        @Test
        fun `요청 타입 조건이 있으면 요청한 타입의 데이터만 조회된다`() {
            //given
            // TODO 알림 타입이 추가되면 테스트 수정
            val shouldContains = listOf(
                notificationRepository.save(Notification.of(NotificationType.ACHIEVEMENT_ACHIEVE, userId, ""))
            )
            val condition = NotificationCondition()
            val pageRequest = PageRequest.of(0, 100)

            //when
            val result =
                notificationRepository.getActiveNotifications(userId, condition, pageRequest)

            //then
            assertThat(result.content).containsAll(shouldContains)
        }

        @DisplayName("반환되는 데이터의 총 개수가 실제 조회 대상 데이터의 수와 일치한다")
        @Test
        fun `반환되는 데이터의 총 개수가 실제 조회 대상 데이터의 수와 일치한다`() {
            //given
            val notification = Notification.of(NotificationType.ACHIEVEMENT_ACHIEVE, userId, "")
            notificationRepository.save(notification)
            notification.confirmNotification()
            val shouldContains = listOf(
                notificationRepository.save(Notification.of(NotificationType.ACHIEVEMENT_ACHIEVE, userId, "")),
                notificationRepository.save(Notification.of(NotificationType.ACHIEVEMENT_ACHIEVE, userId, "")),
                notificationRepository.save(notification),
            )
            notificationRepository.save(Notification.of(NotificationType.ACHIEVEMENT_ACHIEVE, otherUserId, ""))

            val insertQueryHasDeletedDate =
                "insert into notification (user_id, title, content, type, metadata, created_date, confirmed_date, deleted_date) VALUES (?, 't', 'c', 'ACHIEVEMENT_ACHIEVE', '', now(), null, now())"
            with(entityManager.createNativeQuery(insertQueryHasDeletedDate)) {
                setParameter(1, userId)
                executeUpdate()
            }

            val condition = NotificationCondition()
            val pageRequest = PageRequest.of(0, 100)

            //when
            val result =
                notificationRepository.getActiveNotifications(userId, condition, pageRequest)

            //then
            assertThat(result.totalElements).isEqualTo(shouldContains.size.toLong())
        }
    }
}