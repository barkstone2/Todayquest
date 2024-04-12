package dailyquest.notification.entity

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

@DisplayName("알림 엔티티 유닛 테스트")
class NotificationEntityUnitTest {

    @DisplayName("팩토리 메서드 호출 시")
    @Nested
    inner class TestFactoryMethod {
        @DisplayName("content 값을 생략하면 빈 문자열로 객체를 생성한다")
        @Test
        fun `content 값을 생략하면 빈 문자열로 객체를 생성한다`() {
            //given
            //when
            val result = Notification.of(NotificationType.ACHIEVEMENT_ACHIEVE, 1L, "")

            //then
            assertThat(result.content).isEqualTo("")
        }
        
        @DisplayName("content 값을 입력하면 입력된 값으로 객체를 생성한다")
        @Test
        fun `content 값을 입력하면 입력된 값으로 객체를 생성한다`() {
            //given
            val content = "content"
            //when
            val result = Notification.of(NotificationType.ACHIEVEMENT_ACHIEVE, 1L, "", content)

            //then
            assertThat(result.content).isEqualTo(content)
        }

        @DisplayName("metadata 값을 생략하면 빈 맵을 json으로 변환해 객체를 생성한다")
        @Test
        fun `metadata 값을 생략하면 빈 맵을 json으로 변환해 객체를 생성한다`() {
            //given
            //when
            val result = Notification.of(NotificationType.ACHIEVEMENT_ACHIEVE, 1L, "")

            //then
            assertThat(result.metadata).isEqualTo(jacksonObjectMapper().writeValueAsString(emptyMap<String, Any>()))
        }

        @DisplayName("metadata 값을 입력하면 해당 맵을 json으로 변환해 객체를 생성한다")
        @Test
        fun `metadata 값을 입력하면 해당 맵을 json으로 변환해 객체를 생성한다`() {
            //given
            val metadata = mapOf("key1" to "value1", "key2" to "value2")

            //when
            val result = Notification.of(NotificationType.ACHIEVEMENT_ACHIEVE, 1L, "", "", metadata = metadata)

            //then
            assertThat(result.metadata).isEqualTo(jacksonObjectMapper().writeValueAsString(metadata))
        }
    }

    @DisplayName("confirmNotification 호출 시")
    @Nested
    inner class TestConfirmNotification {
        @AfterEach
        fun close() {
            unmockkStatic(LocalDateTime::class)
        }

        @DisplayName("confirmedDate가 null이 아니면 confirmedDate가 변경되지 않는다")
        @Test
        fun `confirmedDate가 null이 아니면 confirmedDate가 변경되지 않는다`() {
            //given
            val notification = Notification.of(NotificationType.ACHIEVEMENT_ACHIEVE, 1L, "")
            notification.confirmNotification()
            val now = LocalDateTime.of(2020, 12, 12, 12, 0)
            mockkStatic(LocalDateTime::class)
            every { LocalDateTime.now() } returns now

            //when
            notification.confirmNotification()

            //then
            assertThat(notification.confirmedDate).isNotEqualTo(now)
        }

        @DisplayName("deletedDate가 null이 아니면 confirmedDate가 변경되지 않는다")
        @Test
        fun `deletedDate가 null이 아니면 confirmedDate가 변경되지 않는다`() {
            //given
            val notification = Notification.of(NotificationType.ACHIEVEMENT_ACHIEVE, 1L, "")
            notification.deleteNotification()
            val now = LocalDateTime.of(2020, 12, 12, 12, 0)
            mockkStatic(LocalDateTime::class)
            every { LocalDateTime.now() } returns now

            //when
            notification.confirmNotification()

            //then
            assertThat(notification.confirmedDate).isNotEqualTo(now)
        }

        @DisplayName("confirmedDate가 null이면 confirmedDate가 현재 시간으로 변경된다")
        @Test
        fun `confirmedDate가 null이면 confirmedDate가 현재 시간으로 변경된다`() {
            //given
            val now = LocalDateTime.of(2020, 12, 12, 12, 0)
            mockkStatic(LocalDateTime::class)
            every { LocalDateTime.now() } returns now
            val notification = Notification.of(NotificationType.ACHIEVEMENT_ACHIEVE, 1L, "")

            //when
            notification.confirmNotification()

            //then
            assertThat(notification.confirmedDate).isEqualTo(now)
        }


    }

    @DisplayName("deleteNotification 호출 시")
    @Nested
    inner class TestDeleteNotification {
        @AfterEach
        fun close() {
            unmockkStatic(LocalDateTime::class)
        }

        @DisplayName("deletedDate가 null이면 현재 시간으로 변경된다")
        @Test
        fun `deletedDate가 null이면 현재 시간으로 변경된다`() {
            //given
            val now = LocalDateTime.of(2020, 12, 12, 12, 0)
            mockkStatic(LocalDateTime::class)
            every { LocalDateTime.now() } returns now
            val notification = Notification.of(NotificationType.ACHIEVEMENT_ACHIEVE, 1L, "")

            //when
            notification.deleteNotification()

            //then
            assertThat(notification.deletedDate).isEqualTo(now)
        }

        @DisplayName("deletedDate가 null이 아니면 변경되지 않는다")
        @Test
        fun `deletedDate가 null이 아니면 변경되지 않는다`() {
            //given
            val notification = Notification.of(NotificationType.ACHIEVEMENT_ACHIEVE, 1L, "")
            notification.deleteNotification()
            val now = LocalDateTime.of(2020, 12, 12, 12, 0)
            mockkStatic(LocalDateTime::class)
            every { LocalDateTime.now() } returns now

            //when
            notification.deleteNotification()

            //then
            assertThat(notification.deletedDate).isNotEqualTo(now)
        }
    }
}