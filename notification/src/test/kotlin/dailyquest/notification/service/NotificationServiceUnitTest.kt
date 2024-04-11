package dailyquest.notification.service

import dailyquest.notification.dto.NotificationCondition
import dailyquest.notification.dto.NotificationResponse
import dailyquest.notification.dto.NotificationSaveRequest
import dailyquest.notification.entity.Notification
import dailyquest.notification.repository.NotificationRepository
import dailyquest.properties.NotificationPageSizeProperties
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest

@ExtendWith(MockKExtension::class)
@DisplayName("알림 서비스 유닛 테스트")
class NotificationServiceUnitTest {

    @InjectMockKs
    private lateinit var notificationService: NotificationService
    @RelaxedMockK
    private lateinit var notificationRepository: NotificationRepository
    @RelaxedMockK
    private lateinit var notificationPageSizeProperties: NotificationPageSizeProperties

    @DisplayName("getNotConfirmedNotificationsOfUser 호출 시")
    @Nested
    inner class TestGetNotConfirmedNotificationsOfUser {
        @DisplayName("condition의 page와 프로퍼티의 pageSize로 생성한 pageRequest로 조회 요청한다")
        @Test
        fun `condition의 page와 프로퍼티의 pageSize로 생성한 pageRequest로 조회 요청한다`() {
            //given
            val userId = 1L
            val page = 1
            val pageSize = 1
            val pageRequest = PageRequest.of(page, pageSize)
            val condition = NotificationCondition(page)
            every { notificationPageSizeProperties.size } returns pageSize

            //when
            notificationService.getNotConfirmedNotificationsOfUser(userId, condition)

            //then
            verify {
                notificationRepository.getNotConfirmedNotifications(eq(userId), eq(condition), eq(pageRequest))
            }
        }

        @DisplayName("조회된 알림 목록을 NotificationResponse 목록으로 변환해 반환한다")
        @Test
        fun `조회된 알림 목록을 NotificationResponse 목록으로 변환해 반환한다`() {
            //given
            val notification: Notification = mockk(relaxed = true)
            every { notification.metadata } returns "{}"
            every { notificationRepository.getNotConfirmedNotifications(any(), any(), any()) } returns PageImpl(listOf(notification))
            mockkObject(NotificationResponse)
            every { notificationPageSizeProperties.size } returns 1

            //when
            notificationService.getNotConfirmedNotificationsOfUser(1L, NotificationCondition())

            //then
            verify {
                NotificationResponse.from(eq(notification))
            }
        }
    }

    @DisplayName("getActiveNotificationsOfUser 호출 시")
    @Nested
    inner class TestGetActiveNotificationsOfUser {
        @DisplayName("condition의 page와 프로퍼티의 pageSize로 생성한 pageRequest로 조회 요청한다")
        @Test
        fun `condition의 page와 프로퍼티의 pageSize로 생성한 pageRequest로 조회 요청한다`() {
            //given
            val userId = 1L
            val page = 1
            val pageSize = 1
            val pageRequest = PageRequest.of(page, pageSize)
            val condition = NotificationCondition(page)
            every { notificationPageSizeProperties.size } returns pageSize

            //when
            notificationService.getActiveNotificationsOfUser(userId, condition)

            //then
            verify {
                notificationRepository.getActiveNotifications(eq(userId), eq(condition), eq(pageRequest))
            }
        }

        @DisplayName("조회된 알림 목록을 NotificationResponse 목록으로 변환해 반환한다")
        @Test
        fun `조회된 알림 목록을 NotificationResponse 목록으로 변환해 반환한다`() {
            //given
            val notification: Notification = mockk(relaxed = true)
            every { notification.metadata } returns "{}"
            every { notificationRepository.getActiveNotifications(any(), any(), any()) } returns PageImpl(listOf(notification))
            mockkObject(NotificationResponse)
            every { notificationPageSizeProperties.size } returns 1

            //when
            notificationService.getActiveNotificationsOfUser(1L, NotificationCondition())

            //then
            verify {
                NotificationResponse.from(eq(notification))
            }
        }
    }

    @DisplayName("saveNotification 호출 시")
    @Nested
    inner class TestSaveNotification {
        @DisplayName("요청 DTO를 엔티티로 변환해 저장 요청한다")
        @Test
        fun `요청 DTO를 엔티티로 변환해 저장 요청한다`() {
            //given
            val saveEntity: Notification = mockk(relaxed = true)
            val saveRequest: NotificationSaveRequest = mockk(relaxed = true)
            every { saveRequest.mapToEntity() } returns saveEntity
            every { notificationRepository.save(any()) } answers { nothing }

            //when
            notificationService.saveNotification(saveRequest, 1L)
            
            //then
            verifyOrder {
                saveRequest.mapToEntity()
                notificationRepository.save(eq(saveEntity))
            }
        }
    }

    @DisplayName("confirmNotification 호출 시")
    @Nested
    inner class TestConfirmNotification {
        @DisplayName("알림 ID와 유저 ID로 조회한 결과가 존재하면 확인 처리 한다")
        @Test
        fun `알림 ID와 유저 ID로 조회한 결과가 존재하면 확인 처리 한다`() {
            //given
            val notification: Notification = mockk(relaxed = true)
            every { notificationRepository.getNotificationByIdAndUserId(any(), any()) } returns notification

            //when
            notificationService.confirmNotification(1L, 1L)

            //then
            verify { notification.confirmNotification() }
        }
    }

    @DisplayName("confirmAllNotifications 호출 시")
    @Nested
    inner class TestConfirmAllNotifications {
        @DisplayName("리포지토리에 모든 알림 확인 요청을 위임한다")
        @Test
        fun `리포지토리에 모든 알림 확인 요청을 위임한다`() {
            //given
            val userId = 1L

            //when
            notificationService.confirmAllNotifications(userId)

            //then
            verify { notificationRepository.confirmAllNotifications(eq(userId)) }
        }
    }

    @DisplayName("deleteNotification 호출 시")
    @Nested
    inner class TestDeleteNotification {
        @DisplayName("알림 ID와 유저 ID로 조회한 결과가 존재하면 삭제 처리 한다")
        @Test
        fun `알림 ID와 유저 ID로 조회한 결과가 존재하면 삭제 처리 한다`() {
            //given
            val notification: Notification = mockk(relaxed = true)
            every { notificationRepository.getNotificationByIdAndUserId(any(), any()) } returns notification
            
            //when
            notificationService.deleteNotification(1L, 1L)
            
            //then
            verify { notification.deleteNotification() }
        }
    }
    

    @DisplayName("deleteAllNotifications 호출 시")
    @Nested
    inner class TestDeleteAllNotifications {
        @DisplayName("리포지토리에 모든 알림 삭제 요청을 위임한다")
        @Test
        fun `리포지토리에 모든 알림 삭제 요청을 위임한다`() {
            //given
            val userId = 1L
            
            //when
            notificationService.deleteAllNotifications(userId)
            
            //then
            verify { notificationRepository.deleteAllNotifications(eq(userId)) }
        }
    }
}