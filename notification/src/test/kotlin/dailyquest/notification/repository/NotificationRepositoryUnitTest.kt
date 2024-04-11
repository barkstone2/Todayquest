package dailyquest.notification.repository

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import dailyquest.notification.entity.Notification
import dailyquest.notification.entity.NotificationType
import jakarta.persistence.EntityManager
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.data.repository.findByIdOrNull
import java.time.LocalDateTime

@DisplayName("알림 리포지토리 유닛 테스트")
@DataJpaTest
class NotificationRepositoryUnitTest {

    @Autowired
    private lateinit var notificationRepository: NotificationRepository

    @Autowired
    private lateinit var entityManager: EntityManager

    private val om = ObjectMapper().registerKotlinModule()

    @DisplayName("엔티티 저장 시 오류가 발생하지 않는다")
    @Test
    fun `엔티티 저장 시 오류가 발생하지 않는다`() {
        //given
        val notification = Notification.of(NotificationType.ACHIEVEMENT_ACHIEVE, 1L, "notification title")

        //when
        //then
        assertDoesNotThrow { notificationRepository.save(notification) }
    }

    @DisplayName("엔티티 저장 시 메타데이터 json 값을 저장해도 오류가 발생하지 않는다")
    @Test
    fun `엔티티 저장 시 메타데이터 json 값을 저장해도 오류가 발생하지 않는다`() {
        //given
        val metadata = mapOf("meta1" to "1", "meta2" to 3, "meta4" to mapOf("a" to "b"))
        val notification = Notification.of(
            NotificationType.ACHIEVEMENT_ACHIEVE,
            1L,
            "notification title",
            "",
            om.writeValueAsString(metadata)
        )

        //when
        //then
        assertDoesNotThrow { notificationRepository.save(notification) }
    }

    @DisplayName("메타데이터 조회 시 json을 맵으로 변환 가능하다")
    @Test
    fun `메타데이터 조회 시 json을 맵으로 변환 가능하다`() {
        //given
        val metadata = mutableMapOf<String, String>()
        val pairs = listOf("key1" to "1", "key2" to 2)
        pairs.forEach { metadata[it.first] = it.second.toString() }

        val notification = Notification.of(NotificationType.ACHIEVEMENT_ACHIEVE, 1L, "notification title", "", om.writeValueAsString(metadata))
        val savedNotification = notificationRepository.saveAndFlush(notification)
        entityManager.clear()
        val foundNotification = notificationRepository.findById(savedNotification.id).get()

        //when
        val metadataMap: Map<String, String> = om.readValue(foundNotification.metadata, jacksonTypeRef())

        //then
        pairs.forEach { assertThat(metadataMap[it.first]).isEqualTo(it.second.toString()) }
    }

    @DisplayName("알림 업데이트 시 오류가 발생하지 않는다")
    @Test
    fun `알림 업데이트 시 오류가 발생하지 않는다`() {
        //given
        val notification = Notification.of(NotificationType.ACHIEVEMENT_ACHIEVE, 1L, "notification title")
        val savedNotification = notificationRepository.saveAndFlush(notification)
        entityManager.clear()
        val foundNotification = notificationRepository.findById(savedNotification.id).get()

        //when
        foundNotification.confirmNotification()
        foundNotification.deleteNotification()

        //then
        assertDoesNotThrow { notificationRepository.saveAndFlush(foundNotification) }
    }

    @DisplayName("getNotificationByIdAndUserId 호출 시")
    @Nested
    inner class TestGetNotificationByIdAndUserId {
        private val userId = 1L
        private val otherUserId = 2L

        @DisplayName("해당 ID의 알림이 존재하고 해당 유저 ID 참조를 가진 경우 알림 엔티티가 반환된다")
        @Test
        fun `해당 ID의 알림이 존재하고 해당 유저 ID 참조를 가진 경우 알림 엔티티가 반환된다`() {
            //given
            val notification = Notification.of(NotificationType.ACHIEVEMENT_ACHIEVE, userId, "")
            notificationRepository.save(notification)

            //when
            val result = notificationRepository.getNotificationByIdAndUserId(notification.id, userId)

            //then
            assertThat(result).isEqualTo(notification)
        }

        @DisplayName("해당 ID의 알림이 존재하고 해당 유저 ID 참조를 가지지 않은 경우 null이 반환된다")
        @Test
        fun `해당 ID의 알림이 존재하고 해당 유저 ID 참조를 가지지 않은 경우 null이 반환된다`() {
            //given
            val notification = Notification.of(NotificationType.ACHIEVEMENT_ACHIEVE, otherUserId, "")
            notificationRepository.save(notification)

            //when
            val result = notificationRepository.getNotificationByIdAndUserId(notification.id, userId)

            //then
            assertThat(result).isNull()
        }

        @DisplayName("해당 ID의 알림이 없으면 null이 반환된다")
        @Test
        fun `해당 ID의 알림이 없으면 null이 반환된다`() {
            //given
            val notification = Notification.of(NotificationType.ACHIEVEMENT_ACHIEVE, userId, "")
            notificationRepository.save(notification)

            //when
            val result = notificationRepository.getNotificationByIdAndUserId(notification.id+1, userId)

            //then
            assertThat(result).isNull()
        }
    }

    @DisplayName("confirmAllNotification 호출 시")
    @Nested
    inner class TestConfirmAllNotification {
        private val userId = 1L
        private val otherUserId = 2L

        @DisplayName("등록된 알림의 userId가 일치하고")
        @Nested
        inner class WhenUserIdMatched {
            @DisplayName("confirmedDate와 deledteDate가 null인 모든 알림의 confiremdDate가 변경된다")
            @Test
            fun `confirmedDate와 deledteDate가 null인 모든 알림의 confiremdDate가 변경된다`() {
                //given
                val savedNotifications = listOf(
                    Notification.of(NotificationType.ACHIEVEMENT_ACHIEVE, userId, ""),
                    Notification.of(NotificationType.ACHIEVEMENT_ACHIEVE, userId, "")
                )
                notificationRepository.saveAll(savedNotifications)

                //when
                notificationRepository.confirmAllNotifications(userId)

                //then
                val result = notificationRepository.findAllById(savedNotifications.map { it.id })
                assertThat(result).allMatch { it.confirmedDate != null }
            }

            @DisplayName("confirmedDate가 null이고 deleteDate가 null이 아니면 confirmedDate가 변경되지 않는다")
            @Test
            fun `confirmedDate가 null이고 deleteDate가 null이 아니면 confirmedDate가 변경되지 않는다`() {
                //given
                val savedNotifications = listOf(
                    Notification.of(NotificationType.ACHIEVEMENT_ACHIEVE, userId, ""),
                    Notification.of(NotificationType.ACHIEVEMENT_ACHIEVE, userId, "")
                )
                notificationRepository.saveAll(savedNotifications)
                savedNotifications.forEach {
                    it.deleteNotification()
                    notificationRepository.save(it)
                }

                //when
                notificationRepository.confirmAllNotifications(userId)

                //then
                val result = notificationRepository.findAllById(savedNotifications.map { it.id })
                assertThat(result).allMatch { it.confirmedDate == null }
            }

            @DisplayName("confirmedDate가 null이 아니고 deleteDate가 null이면 confirmedDate가 변경되지 않는다")
            @Test
            fun `confirmedDate가 null이 아니고 deleteDate가 null이면 confirmedDate가 변경되지 않는다`() {
                //given
                val confirmedDate = LocalDateTime.of(2000, 12, 12, 12, 0)
                val notificationIds = listOf(1L, 2L)
                val query =
                    entityManager.createNativeQuery("insert into notification (notification_id, user_id, title, content, type, metadata, created_date, confirmed_date, deleted_date) VALUES (?, ?, 't', 'c', 'ACHIEVEMENT_ACHIEVE', '', now(), ?, null)")
                query.setParameter(2, userId)
                query.setParameter(3, confirmedDate)
                notificationIds.forEach {
                    query.setParameter(1, it)
                    query.executeUpdate()
                }

                //when
                notificationRepository.confirmAllNotifications(userId)

                //then
                val result = notificationRepository.findAllById(notificationIds)
                assertThat(result).allMatch { it.confirmedDate == confirmedDate }
            }
        }

        @DisplayName("등록된 알림의 userId가 일치하지 않으면 confirmedDate가 변경되지 않는다")
        @Test
        fun `등록된 알림의 userId가 일치하지 않으면 confirmedDate가 변경되지 않는다`() {
            //given
            val notification = Notification.of(NotificationType.ACHIEVEMENT_ACHIEVE, otherUserId, "")
            notificationRepository.save(notification)

            //when
            notificationRepository.confirmAllNotifications(userId)

            //then
            val result = notificationRepository.findByIdOrNull(notification.id) ?: fail("result should not be null")
            assertThat(result.confirmedDate).isNull()
        }
    }
}