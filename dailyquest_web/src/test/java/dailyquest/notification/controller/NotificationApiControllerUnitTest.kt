package dailyquest.notification.controller

import com.ninjasquad.springmockk.MockkBean
import dailyquest.annotation.WebMvcUnitTest
import dailyquest.notification.dto.NotificationCondition
import dailyquest.notification.entity.NotificationType
import dailyquest.notification.service.NotificationService
import dailyquest.user.dto.UserPrincipal
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.patch

@ExtendWith(MockKExtension::class)
@WebMvcUnitTest([NotificationApiController::class])
class NotificationApiControllerUnitTest @Autowired constructor(
    private val mvc: MockMvc,
) {
    @MockkBean(relaxed = true)
    private lateinit var notificationService: NotificationService

    private val urlPrefix = "/api/v1/notifications"

    @DisplayName("getNotConfirmedNotifications 호출 시")
    @Nested
    inner class TestGetNotConfirmedNotifications {
        @DisplayName("요청 타입과 페이지에 맞는 목록을 조회한다")
        @Test
        fun `요청 타입과 페이지에 맞는 목록을 조회한다`() {
            //given
            val url = "$urlPrefix/not-confirmed"
            val page = 1
            val type = NotificationType.ACHIEVEMENT_ACHIEVE
            val condition = NotificationCondition(page, type)
            every { notificationService.getNotConfirmedNotificationsOfUser(any(), any()) } returns Page.empty()

            //when
            mvc.get(url) {
                this.param("page", page.toString())
                this.param("type", type.toString())
            }

            //then
            verify {
                notificationService.getNotConfirmedNotificationsOfUser(any(), eq(condition))
            }
        }
    }

    @DisplayName("getActiveNotifications 호출 시")
    @Nested
    inner class TestGetActiveNotifications {
        @DisplayName("요청 타입과 페이지에 맞는 목록을 조회한다")
        @Test
        fun `요청 타입과 페이지에 맞는 목록을 조회한다`() {
            //given
            val url = urlPrefix
            val page = 1
            val type = NotificationType.ACHIEVEMENT_ACHIEVE
            val condition = NotificationCondition(page, type)
            every { notificationService.getActiveNotificationsOfUser(any(), any()) } returns Page.empty()

            //when
            mvc.get(url) {
                this.param("page", page.toString())
                this.param("type", type.toString())
            }

            //then
            verify {
                notificationService.getActiveNotificationsOfUser(any(), eq(condition))
            }
        }
    }

    @DisplayName("confirmNotification 호출 시")
    @Nested
    inner class TestConfirmNotification {
        @DisplayName("요청 URL의 ID에 대해 확인 요청한다")
        @Test
        fun `요청 URL의 ID에 대해 확인 요청한다`() {
            //given
            val notificationId = 1L
            val url = "$urlPrefix/$notificationId/confirm"

            //when
            mvc.patch(url) {
                this.with(csrf())
            }

            //then
            verify {
                notificationService.confirmNotification(eq(notificationId), any())
            }
        }
    }

    @DisplayName("confirmAllNotifications 호출 시")
    @Nested
    inner class TestConfirmAllNotifications {
        @DisplayName("유저의 모든 알림에 대해 확인 요청한다")
        @Test
        fun `유저의 모든 알림에 대해 확인 요청한다`() {
            //given
            val securityContext = SecurityContextHolder.getContext()
            val authentication = securityContext.authentication
            val userPrincipal = authentication.principal as UserPrincipal
            val userId = userPrincipal.id
            val url = "$urlPrefix/confirm-all"

            //when
            mvc.patch(url) {
                this.with(csrf())
            }

            //then
            verify {
                notificationService.confirmAllNotifications(eq(userId))
            }
        }
    }

    @DisplayName("deleteNotification 호출 시")
    @Nested
    inner class TestDeleteNotification {
        @DisplayName("요청 URL의 ID에 대해 삭제 요청한다")
        @Test
        fun `요청 URL의 ID에 대해 삭제 요청한다`() {
            //given
            val notificationId = 1L
            val url = "$urlPrefix/$notificationId/delete"

            //when
            mvc.patch(url) {
                this.with(csrf())
            }

            //then
            verify {
                notificationService.deleteNotification(eq(notificationId), any())
            }
        }
    }

    @DisplayName("deleteAllNotifications 호출 시")
    @Nested
    inner class TestDeleteAllNotifications {
        @DisplayName("유저의 모든 알림에 대해 삭제 요청한다")
        @Test
        fun `유저의 모든 알림에 대해 삭제 요청한다`() {
            //given
            val securityContext = SecurityContextHolder.getContext()
            val authentication = securityContext.authentication
            val userPrincipal = authentication.principal as UserPrincipal
            val userId = userPrincipal.id
            val url = "$urlPrefix/delete-all"

            //when
            mvc.patch(url) {
                this.with(csrf())
            }

            //then
            verify {
                notificationService.deleteAllNotifications(eq(userId))
            }
        }
    }
}