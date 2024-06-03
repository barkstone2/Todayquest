package dailyquest.notification.controller

import com.ninjasquad.springmockk.SpykBean
import dailyquest.context.IntegrationTestContext
import dailyquest.context.MockElasticsearchTestContextConfig
import dailyquest.context.MockRedisTestContextConfig
import dailyquest.notification.entity.Notification
import dailyquest.notification.entity.NotificationType
import dailyquest.notification.repository.NotificationRepository
import dailyquest.properties.NotificationPageSizeProperties
import io.mockk.every
import io.mockk.junit5.MockKExtension
import jakarta.persistence.EntityManager
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.hamcrest.Matchers
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.not
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.data.repository.findByIdOrNull
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.patch
import java.time.LocalDateTime

@Import(MockElasticsearchTestContextConfig::class, MockRedisTestContextConfig::class)
@ExtendWith(MockKExtension::class)
@DisplayName("알림 API 컨트롤러 통합 테스트")
class NotificationApiControllerTest @Autowired constructor(
    private val notificationRepository: NotificationRepository,
    private val entityManager: EntityManager,
    @SpykBean
    private val notificationPageSizeProperties: NotificationPageSizeProperties
) : IntegrationTestContext() {

    private val uriPrefix = "/api/v1/notifications"

    @DisplayName("확인하지 않은 알림 목록 조회 시")
    @Nested
    inner class TestGetNotConfirmedNotifications {
        private val url = "$uriPrefix/not-confirmed"

        @DisplayName("다른 유저의 알림은 조회되지 않는다")
        @Test
        fun `다른 유저의 알림은 조회되지 않는다`() {
            //given
            val anotherUserNotification = Notification.of(NotificationType.ACHIEVEMENT_ACHIEVE, anotherUser.id, "")
            notificationRepository.save(anotherUserNotification)

            //when
            val result = mvc.get(url) {
                useUserConfiguration()
            }

            //then
            result.andExpect {
                status { isOk() }
                jsonPath("$.data.content.*.id") {
                    value(not(contains(anotherUserNotification.id.toInt())))
                }
            }
        }
        
        @DisplayName("삭제된 알림은 조회되지 않는다")
        @Test
        fun `삭제된 알림은 조회되지 않는다`() {
            //given
            val deletedNotification = Notification.of(NotificationType.ACHIEVEMENT_ACHIEVE, user.id, "")
            notificationRepository.save(deletedNotification)
            deletedNotification.deleteNotification()
            notificationRepository.save(deletedNotification)

            //when
            val result = mvc.get(url) {
                useUserConfiguration()
            }

            //then
            result.andExpect {
                status { isOk() }
                jsonPath("$.data.content.*.id") {
                    value(not(contains(deletedNotification.id.toInt())))
                }
            }
        }

        @DisplayName("이미 확인된 알림은 조회되지 않는다")
        @Test
        fun `이미 확인된 알림은 조회되지 않는다`() {
            //given
            val confirmedNotification = Notification.of(NotificationType.ACHIEVEMENT_ACHIEVE, user.id, "")
            notificationRepository.save(confirmedNotification)
            confirmedNotification.confirmNotification()
            notificationRepository.save(confirmedNotification)

            //when
            val result = mvc.get(url) {
                useUserConfiguration()
            }

            //then
            result.andExpect {
                status { isOk() }
                jsonPath("$.data.content.*.id") {
                    value(not(contains(confirmedNotification.id.toInt())))
                }
            }
        }

        // TODO 요청 타입과 다른 타입의 알림은 조회되지 않는다
        @DisplayName("요청 타입과 다른 타입의 알림은 조회되지 않는다")
        @Test
        fun `요청 타입과 다른 타입의 알림은 조회되지 않는다`() {
            //given
            val otherTypeNotification = Notification.of(NotificationType.ACHIEVEMENT_ACHIEVE, user.id, "")
            notificationRepository.save(otherTypeNotification)

            //when
            val result = mvc.get(url) {
                useUserConfiguration()
                // TODO 새로운 타입 추가되면 테스트 코드 수정하기
                param("type", NotificationType.ACHIEVEMENT_ACHIEVE.name)
            }

            //then
            return // TODO 테스트 코드 수정 시 return 제거하기
            @Suppress("UNREACHABLE_CODE")
            result.andExpect {
                status { isOk() }
                jsonPath("$.data.content.*.id") {
                    value(not(contains(otherTypeNotification.id.toInt())))
                }
            }
        }

        @DisplayName("확인되지 않은 알림이 조회된다")
        @Test
        fun `확인되지 않은 알림이 조회된다`() {
            //given
            val notConfirmedNotification = Notification.of(NotificationType.ACHIEVEMENT_ACHIEVE, user.id, "")
            notificationRepository.save(notConfirmedNotification)

            //when
            val result = mvc.get(url) {
                useUserConfiguration()
            }

            //then
            result.andExpect {
                status { isOk() }
                jsonPath("$.data.content.*.id") {
                    value(contains(notConfirmedNotification.id.toInt()))
                }
            }
        }

        @DisplayName("요청한 페이지의 알림만 조회된다")
        @Test
        fun `요청한 페이지의 알림만 조회된다`() {
            //given
            val pageNo = 0
            val idx = 2 - pageNo
            val notifications = listOf(
                notificationRepository.save(Notification.of(NotificationType.ACHIEVEMENT_ACHIEVE, user.id, "")),
                notificationRepository.save(Notification.of(NotificationType.ACHIEVEMENT_ACHIEVE, user.id, "")),
                notificationRepository.save(Notification.of(NotificationType.ACHIEVEMENT_ACHIEVE, user.id, "")),
            )
            every { notificationPageSizeProperties.size } returns 1

            //when
            val result = mvc.get(url) {
                useUserConfiguration()
                param("page", pageNo.toString())
            }

            //then
            result.andExpect {
                status { isOk() }
                jsonPath("$.data.content.size()") { value(1) }
                jsonPath("$.data.content.*.id") {
                    value(contains(notifications[idx].id.toInt()))
                }
            }
        }

        @DisplayName("최근에 저장된 알림 순으로 조회된다")
        @Test
        fun `최근에 저장된 알림 순으로 조회된다`() {
            //given
            val notifications = listOf(
                notificationRepository.save(Notification.of(NotificationType.ACHIEVEMENT_ACHIEVE, user.id, "")),
                notificationRepository.save(Notification.of(NotificationType.ACHIEVEMENT_ACHIEVE, user.id, "")),
                notificationRepository.save(Notification.of(NotificationType.ACHIEVEMENT_ACHIEVE, user.id, "")),
            )

            //when
            val result = mvc.get(url) {
                useUserConfiguration()
            }

            //then
            result.andExpect {
                status { isOk() }
                jsonPath("$.data.content.*.id") {
                    value(Matchers.containsInRelativeOrder(notifications.reversed().map { it.id.toInt() }
                        .reduce { _, i -> i }))
                }
            }
        }
    }

    @DisplayName("활성화 상태의 알림 목록 조회 시")
    @Nested
    inner class TestGetActiveNotifications {
        private val url = uriPrefix

        @DisplayName("다른 유저의 알림은 조회되지 않는다")
        @Test
        fun `다른 유저의 알림은 조회되지 않는다`() {
            //given
            val anotherUserNotification = Notification.of(NotificationType.ACHIEVEMENT_ACHIEVE, anotherUser.id, "")
            notificationRepository.save(anotherUserNotification)

            //when
            val result = mvc.get(url) {
                useUserConfiguration()
            }

            //then
            result.andExpect {
                status { isOk() }
                jsonPath("$.data.content.*.id") {
                    value(not(contains(anotherUserNotification.id.toInt())))
                }
            }
        }

        @DisplayName("삭제된 알림은 조회되지 않는다")
        @Test
        fun `삭제된 알림은 조회되지 않는다`() {
            //given
            val deletedNotification = Notification.of(NotificationType.ACHIEVEMENT_ACHIEVE, user.id, "")
            notificationRepository.save(deletedNotification)
            deletedNotification.deleteNotification()
            notificationRepository.save(deletedNotification)

            //when
            val result = mvc.get(url) {
                useUserConfiguration()
            }

            //then
            result.andExpect {
                status { isOk() }
                jsonPath("$.data.content.*.id") {
                    value(not(contains(deletedNotification.id.toInt())))
                }
            }
        }

        @DisplayName("이미 확인된 알림도 조회된다")
        @Test
        fun `이미 확인된 알림도 조회된다`() {
            //given
            val confirmedNotification = Notification.of(NotificationType.ACHIEVEMENT_ACHIEVE, user.id, "")
            notificationRepository.save(confirmedNotification)
            confirmedNotification.confirmNotification()
            notificationRepository.save(confirmedNotification)

            //when
            val result = mvc.get(url) {
                useUserConfiguration()
            }

            //then
            result.andExpect {
                status { isOk() }
                jsonPath("$.data.content.*.id") {
                    value(contains(confirmedNotification.id.toInt()))
                }
            }
        }

        // TODO 요청 타입과 다른 타입의 알림은 조회되지 않는다
        @DisplayName("요청 타입과 다른 타입의 알림은 조회되지 않는다")
        @Test
        fun `요청 타입과 다른 타입의 알림은 조회되지 않는다`() {
            //given
            val otherTypeNotification = Notification.of(NotificationType.ACHIEVEMENT_ACHIEVE, user.id, "")
            notificationRepository.save(otherTypeNotification)

            //when
            val result = mvc.get(url) {
                useUserConfiguration()
                // TODO 새로운 타입 추가되면 테스트 코드 수정하기
                param("type", NotificationType.ACHIEVEMENT_ACHIEVE.name)
            }

            //then
            return // TODO 테스트 코드 수정 시 return 제거하기
            @Suppress("UNREACHABLE_CODE")
            result.andExpect {
                status { isOk() }
                jsonPath("$.data.content.*.id") {
                    value(not(contains(otherTypeNotification.id.toInt())))
                }
            }
        }

        @DisplayName("요청한 페이지의 알림만 조회된다")
        @Test
        fun `요청한 페이지의 알림만 조회된다`() {
            //given
            val pageNo = 0
            val idx = 2 - pageNo
            val notifications = listOf(
                notificationRepository.save(Notification.of(NotificationType.ACHIEVEMENT_ACHIEVE, user.id, "")),
                notificationRepository.save(Notification.of(NotificationType.ACHIEVEMENT_ACHIEVE, user.id, "")),
                notificationRepository.save(Notification.of(NotificationType.ACHIEVEMENT_ACHIEVE, user.id, "")),
            )
            every { notificationPageSizeProperties.size } returns 1

            //when
            val result = mvc.get(url) {
                useUserConfiguration()
                param("page", pageNo.toString())
            }

            //then
            result.andExpect {
                status { isOk() }
                jsonPath("$.data.content.size()") { value(1) }
                jsonPath("$.data.content.*.id") {
                    value(contains(notifications[idx].id.toInt()))
                }
            }
        }

        @DisplayName("최근에 저장된 알림 순으로 조회된다")
        @Test
        fun `최근에 저장된 알림 순으로 조회된다`() {
            //given
            val notifications = listOf(
                notificationRepository.save(Notification.of(NotificationType.ACHIEVEMENT_ACHIEVE, user.id, "")),
                notificationRepository.save(Notification.of(NotificationType.ACHIEVEMENT_ACHIEVE, user.id, "")),
                notificationRepository.save(Notification.of(NotificationType.ACHIEVEMENT_ACHIEVE, user.id, "")),
            )

            //when
            val result = mvc.get(url) {
                useUserConfiguration()
            }

            //then
            result.andExpect {
                status { isOk() }
                jsonPath("$.data.content.*.id") {
                    value(Matchers.containsInRelativeOrder(notifications.reversed().map { it.id.toInt() }
                        .reduce { _, i -> i }))
                }
            }
        }
    }

    @DisplayName("알림 확인 요청 시")
    @Nested
    inner class TestConfirmNotification {
        private val url = "$uriPrefix/%s/confirm"

        @DisplayName("다른 유저의 알림은 확인되지 않는다")
        @Test
        fun `다른 유저의 알림은 확인되지 않는다`() {
            //given
            val anotherUserNotification = Notification.of(NotificationType.ACHIEVEMENT_ACHIEVE, anotherUser.id, "")
            notificationRepository.save(anotherUserNotification)

            //when
            mvc.patch(url.format(anotherUserNotification.id)) {
                useUserConfiguration()
            }.andExpect { status { isOk() } }

            //then
            val result = notificationRepository.findByIdOrNull(anotherUserNotification.id) ?: fail("")
            assertThat(result.confirmedDate).isNull()
        }

        @DisplayName("삭제된 알림은 확인되지 않는다")
        @Test
        fun `삭제된 알림은 확인되지 않는다`() {
            //given
            val deletedNotification = Notification.of(NotificationType.ACHIEVEMENT_ACHIEVE, user.id, "")
            notificationRepository.save(deletedNotification)
            deletedNotification.deleteNotification()
            notificationRepository.save(deletedNotification)

            //when
            mvc.patch(url.format(deletedNotification.id)) {
                useUserConfiguration()
            }.andExpect { status { isOk() } }

            //then
            val result = notificationRepository.findByIdOrNull(deletedNotification.id) ?: fail("")
            assertThat(result.confirmedDate).isNull()
        }

        @DisplayName("이미 확인된 알림은 확인되지 않는다")
        @Test
        fun `이미 확인된 알림은 확인되지 않는다`() {
            //given
            val confirmedNotification = Notification.of(NotificationType.ACHIEVEMENT_ACHIEVE, user.id, "")
            notificationRepository.save(confirmedNotification)
            confirmedNotification.confirmNotification()
            notificationRepository.save(confirmedNotification)
            val beforeConfirmedDate = confirmedNotification.confirmedDate

            //when
            mvc.patch(url.format(confirmedNotification.id)) {
                useUserConfiguration()
            }.andExpect { status { isOk() } }

            //then
            val result = notificationRepository.findByIdOrNull(confirmedNotification.id) ?: fail("")
            assertThat(result.confirmedDate).isEqualTo(beforeConfirmedDate)
        }

        @DisplayName("알림이 확인 상태로 변경된다")
        @Test
        fun `알림이 확인 상태로 변경된다`() {
            //given
            val notification = Notification.of(NotificationType.ACHIEVEMENT_ACHIEVE, user.id, "")
            notificationRepository.save(notification)

            //when
            mvc.patch(url.format(notification.id)) {
                useUserConfiguration()
            }.andExpect { status { isOk() } }

            //then
            val result = notificationRepository.findByIdOrNull(notification.id) ?: fail("")
            assertThat(result.confirmedDate).isNotNull()
        }
    }

    @DisplayName("전체 알림 확인 요청 시")
    @Nested
    inner class TestConfirmAllNotifications {
        private val url = "$uriPrefix/confirm-all"
        
        @DisplayName("다른 유저의 알림은 확인되지 않는다")
        @Test
        fun `다른 유저의 알림은 확인되지 않는다`() {
            //given
            val anotherUserNotification = Notification.of(NotificationType.ACHIEVEMENT_ACHIEVE, anotherUser.id, "")
            notificationRepository.save(anotherUserNotification)

            //when
            mvc.patch(url) {
                useUserConfiguration()
            }.andExpect { status { isOk() } }

            //then
            val result = notificationRepository.findByIdOrNull(anotherUserNotification.id) ?: fail("")
            assertThat(result.confirmedDate).isNull()
        }

        @DisplayName("삭제된 알림은 확인되지 않는다")
        @Test
        fun `삭제된 알림은 확인되지 않는다`() {
            //given
            val deletedNotification = Notification.of(NotificationType.ACHIEVEMENT_ACHIEVE, user.id, "")
            notificationRepository.save(deletedNotification)
            deletedNotification.deleteNotification()
            notificationRepository.save(deletedNotification)

            //when
            mvc.patch(url) {
                useUserConfiguration()
            }.andExpect { status { isOk() } }

            //then
            val result = notificationRepository.findByIdOrNull(deletedNotification.id) ?: fail("")
            assertThat(result.confirmedDate).isNull()
        }

        @DisplayName("이미 확인된 알림은 확인되지 않는다")
        @Test
        fun `이미 확인된 알림은 확인되지 않는다`() {
            //given
            val notificationId = 1L
            val beforeConfirmedDate = LocalDateTime.of(2020, 12, 12, 12, 0)
            val query =
                entityManager.createNativeQuery("insert into notification (notification_id, user_id, title, content, type, metadata, created_date, confirmed_date, deleted_date) values (?, ?, '', '', 'ACHIEVEMENT_ACHIEVE', '{}', now(), ?, null)")
            query.setParameter(1, notificationId)
            query.setParameter(2, user.id)
            query.setParameter(3, beforeConfirmedDate)
            query.executeUpdate()

            //when
            mvc.patch(url) {
                useUserConfiguration()
            }.andExpect { status { isOk() } }

            //then
            val result = notificationRepository.findByIdOrNull(notificationId) ?: fail("")
            assertThat(result.confirmedDate).isEqualTo(beforeConfirmedDate)
        }

        @DisplayName("확인되지 않은 모든 알림이 확인 상태로 변경된다")
        @Test
        fun `확인되지 않은 모든 알림이 확인 상태로 변경된다`() {
            //given
            val notifications = listOf(
                notificationRepository.save(Notification.of(NotificationType.ACHIEVEMENT_ACHIEVE, user.id, "")),
                notificationRepository.save(Notification.of(NotificationType.ACHIEVEMENT_ACHIEVE, user.id, "")),
                notificationRepository.save(Notification.of(NotificationType.ACHIEVEMENT_ACHIEVE, user.id, "")),
            )

            //when
            mvc.patch(url) {
                useUserConfiguration()
            }.andExpect { status { isOk() } }

            //then
            val result = notificationRepository.findAllById(notifications.map { it.id })
            assertThat(result).allMatch { it.confirmedDate != null }
        }
    }

    @DisplayName("알림 삭제 요청 시")
    @Nested
    inner class TestDeleteNotification {
        private val url = "$uriPrefix/%s/delete"

        @DisplayName("다른 유저의 알림은 삭제되지 않는다")
        @Test
        fun `다른 유저의 알림은 삭제되지 않는다`() {
            //given
            val anotherUserNotification = Notification.of(NotificationType.ACHIEVEMENT_ACHIEVE, anotherUser.id, "")
            notificationRepository.save(anotherUserNotification)

            //when
            mvc.patch(url.format(anotherUserNotification.id)) {
                useUserConfiguration()
            }.andExpect { status { isOk() } }

            //then
            val result = notificationRepository.findByIdOrNull(anotherUserNotification.id) ?: fail("")
            assertThat(result.deletedDate).isNull()
        }

        @DisplayName("이미 삭제된 알림은 삭제되지 않는다")
        @Test
        fun `이미 삭제된 알림은 삭제되지 않는다`() {
            //given
            val deletedNotification = Notification.of(NotificationType.ACHIEVEMENT_ACHIEVE, user.id, "")
            notificationRepository.save(deletedNotification)
            deletedNotification.deleteNotification()
            notificationRepository.save(deletedNotification)
            val beforeDeletedDate = deletedNotification.deletedDate

            //when
            mvc.patch(url.format(deletedNotification.id)) {
                useUserConfiguration()
            }.andExpect { status { isOk() } }

            //then
            val result = notificationRepository.findByIdOrNull(deletedNotification.id) ?: fail("")
            assertThat(result.deletedDate).isEqualTo(beforeDeletedDate)
        }

        @DisplayName("알림이 삭제 상태로 변경된다")
        @Test
        fun `알림이 삭제 상태로 변경된다`() {
            //given
            val notification = Notification.of(NotificationType.ACHIEVEMENT_ACHIEVE, user.id, "")
            notificationRepository.save(notification)

            //when
            mvc.patch(url.format(notification.id)) {
                useUserConfiguration()
            }.andExpect { status { isOk() } }

            //then
            val result = notificationRepository.findByIdOrNull(notification.id) ?: fail("")
            assertThat(result.deletedDate).isNotNull()
        }

        @DisplayName("확인된 알림도 삭제 상태로 변경된다")
        @Test
        fun `확인된 알림도 삭제 상태로 변경된다`() {
            //given
            val confirmedNotification = Notification.of(NotificationType.ACHIEVEMENT_ACHIEVE, user.id, "")
            notificationRepository.save(confirmedNotification)
            confirmedNotification.confirmNotification()
            notificationRepository.save(confirmedNotification)

            //when
            mvc.patch(url.format(confirmedNotification.id)) {
                useUserConfiguration()
            }.andExpect { status { isOk() } }

            //then
            val result = notificationRepository.findByIdOrNull(confirmedNotification.id) ?: fail("")
            assertThat(result.deletedDate).isNotNull()
        }
    }

    @DisplayName("모든 알림 삭제 요청 시")
    @Nested
    inner class TestDeleteAllNotifications {
        private val url = "$uriPrefix/delete-all"

        @DisplayName("다른 유저의 알림은 삭제되지 않는다")
        @Test
        fun `다른 유저의 알림은 삭제되지 않는다`() {
            //given
            val anotherUserNotification = Notification.of(NotificationType.ACHIEVEMENT_ACHIEVE, anotherUser.id, "")
            notificationRepository.save(anotherUserNotification)

            //when
            mvc.patch(url) {
                useUserConfiguration()
            }.andExpect { status { isOk() } }

            //then
            val result = notificationRepository.findByIdOrNull(anotherUserNotification.id) ?: fail("")
            assertThat(result.deletedDate).isNull()
        }

        @DisplayName("이미 삭제된 알림은 삭제되지 않는다")
        @Test
        fun `이미 삭제된 알림은 삭제되지 않는다`() {
            //given
            val notificationId = 1L
            val beforeDeletedDate = LocalDateTime.of(2020, 12, 12, 12, 0)
            val query =
                entityManager.createNativeQuery("insert into notification (notification_id, user_id, title, content, type, metadata, created_date, confirmed_date, deleted_date) values (?, ?, '', '', 'ACHIEVEMENT_ACHIEVE', '{}', now(), null, ?)")
            query.setParameter(1, notificationId)
            query.setParameter(2, user.id)
            query.setParameter(3, beforeDeletedDate)
            query.executeUpdate()

            //when
            mvc.patch(url) {
                useUserConfiguration()
            }.andExpect { status { isOk() } }

            //then
            val result = notificationRepository.findByIdOrNull(notificationId) ?: fail("")
            assertThat(result.deletedDate).isEqualTo(beforeDeletedDate)
        }

        @DisplayName("삭제되지 않은 모든 알림이 삭제 상태로 변경된다")
        @Test
        fun `삭제되지 않은 모든 알림이 삭제 상태로 변경된다`() {
            //given
            val notifications = listOf(
                notificationRepository.save(Notification.of(NotificationType.ACHIEVEMENT_ACHIEVE, user.id, "")),
                notificationRepository.save(Notification.of(NotificationType.ACHIEVEMENT_ACHIEVE, user.id, "")),
                notificationRepository.save(Notification.of(NotificationType.ACHIEVEMENT_ACHIEVE, user.id, "")),
            )

            //when
            mvc.patch(url) {
                useUserConfiguration()
            }.andExpect { status { isOk() } }

            //then
            val result = notificationRepository.findAllById(notifications.map { it.id })
            assertThat(result).allMatch { it.deletedDate != null }
        }

        @DisplayName("확인된 알림도 삭제 상태로 변경된다")
        @Test
        fun `확인된 알림도 삭제 상태로 변경된다`() {
            //given
            val confirmedNotification = Notification.of(NotificationType.ACHIEVEMENT_ACHIEVE, user.id, "")
            notificationRepository.save(confirmedNotification)
            confirmedNotification.confirmNotification()
            notificationRepository.save(confirmedNotification)

            //when
            mvc.patch(url) {
                useUserConfiguration()
            }.andExpect { status { isOk() } }

            //then
            val result = notificationRepository.findByIdOrNull(confirmedNotification.id) ?: fail("")
            assertThat(result.deletedDate).isNotNull()
        }

    }
}