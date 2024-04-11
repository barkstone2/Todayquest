package dailyquest.notification.properties

import dailyquest.properties.NotificationPageSizeProperties
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("알림 페이지 사이즈 프로퍼티 유닛 테스트")
class NotificationPageSizePropertiesUniTest {

    @DisplayName("객체 생성 시")
    @Nested
    inner class TestCreateObject {
        @DisplayName("pageSize가 지정되지 않으면 기본값인 10으로 객체가 생성된다")
        @Test
        fun `pageSize가 지정되지 않으면 기본값인 10으로 객체가 생성된다`() {
            //given
            //when
            val sizeProperties = NotificationPageSizeProperties()

            //then
            assertThat(sizeProperties.size).isEqualTo(10)
        }
    }
}