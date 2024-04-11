package dailyquest.notification.dto

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dailyquest.notification.entity.NotificationType
import io.mockk.spyk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("알림 저장 요청 DTO 유닛 테스트")
class NotificationSaveRequestUnitTest {

    @DisplayName("mapToEntity 호출 시")
    @Nested
    inner class TestMapToEntity {
        @DisplayName("인터페이스 내부의 메서드를 호출해 알림 엔티티를 생성한다")
        @Test
        fun `인터페이스 내부의 메서드를 호출해 알림 엔티티를 생성한다`() {
            //given
            val type = NotificationType.ACHIEVEMENT_ACHIEVE
            val content = "content"
            val metadata = mapOf("key" to "value")
            val saveRequest = object : NotificationSaveRequest {
                override val notificationType: NotificationType = type
                override val userId: Long = 1L
                override fun createNotificationContent(): String = content
                override fun createNotificationMetadata(): Map<String, Any> = metadata
            }
            val spyRequest = spyk<NotificationSaveRequest>(saveRequest)

            //when
            val result = spyRequest.mapToEntity()

            //then
            assertThat(result.type).isEqualTo(type)
            assertThat(result.title).isEqualTo(type.title)
            assertThat(result.content).isEqualTo(content)
            assertThat(result.metadata).isEqualTo(jacksonObjectMapper().writeValueAsString(metadata))
        }
    }
}