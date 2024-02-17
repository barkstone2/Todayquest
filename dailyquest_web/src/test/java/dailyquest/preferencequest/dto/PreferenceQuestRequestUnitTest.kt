package dailyquest.preferencequest.dto

import dailyquest.common.MessageUtil
import dailyquest.common.DeadLineBoundaryResolver
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Answers
import org.mockito.MockedStatic
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.mock
import java.time.Clock
import java.time.LocalDateTime
import java.time.ZoneOffset

@ExtendWith(MockitoExtension::class)
@DisplayName("선호 퀘스트 요청 DTO 단위 테스트")
class PreferenceQuestRequestUnitTest {

    private val baseTime = LocalDateTime.of(2022, 12, 12, 12, 0, 0)
    private val clock = Clock.fixed(baseTime.toInstant(ZoneOffset.ofHours(9)), ZoneOffset.systemDefault())
    private val boundaryResolver = DeadLineBoundaryResolver(clock = clock)
    private lateinit var messageUtil: MockedStatic<MessageUtil>

    @BeforeEach
    fun init() {
        messageUtil = Mockito.mockStatic(MessageUtil::class.java, Answers.RETURNS_SMART_NULLS)
    }

    @AfterEach
    fun afterEach() {
        messageUtil.close()
    }

    @DisplayName("데드라인 범위 체크 시")
    @Nested
    inner class TestCheckDeadLineRange {

        @DisplayName("데드라인이 minBoundary 이전인 경우 false를 반환한다")
        @Test
        fun `데드라인이 minBoundary 이전인 경우 false를 반환한다`() {
            //given
            val minBoundary = boundaryResolver.resolveMinBoundary()
            val deadLine = minBoundary.minusMinutes(1)
            val preferenceQuestRequest = PreferenceQuestRequest(title = "", deadLine = deadLine, boundaryResolver = boundaryResolver)

            //when

            //then
            assertThat(preferenceQuestRequest.isValidDeadLine()).isFalse()
        }

        @DisplayName("데드라인이 minBoundary와 동일한 경우 false를 반환한다")
        @Test
        fun `데드라인이 minBoundary와 동일한 경우 false를 반환한다`() {
            //given
            val minBoundary = boundaryResolver.resolveMinBoundary()
            val deadLine = minBoundary
            val preferenceQuestRequest = PreferenceQuestRequest(title = "", deadLine = deadLine, boundaryResolver = boundaryResolver)

            //when

            //then
            assertThat(preferenceQuestRequest.isValidDeadLine()).isFalse()
        }

        @DisplayName("데드라인이 minBoundary 이후인 경우 true를 반환한다")
        @Test
        fun `데드라인이 minBoundary 이후인 경우 true를 반환한다`() {
            //given
            val minBoundary = boundaryResolver.resolveMinBoundary()
            val deadLine = minBoundary.plusMinutes(1)
            val preferenceQuestRequest = PreferenceQuestRequest(title = "", deadLine = deadLine, boundaryResolver = boundaryResolver)

            //when

            //then
            assertThat(preferenceQuestRequest.isValidDeadLine()).isTrue()
        }

        @DisplayName("초 단위 차이는 결과에 영향을 미치지 않는다")
        @Test
        fun `초 단위 차이는 결과에 영향을 미치지 않는다`() {
            //given
            val minBoundary = boundaryResolver.resolveMinBoundary()
            val deadLine = minBoundary.plusSeconds(1)
            val preferenceQuestRequest = PreferenceQuestRequest(title = "", deadLine = deadLine, boundaryResolver = boundaryResolver)

            //when

            //then
            assertThat(preferenceQuestRequest.isValidDeadLine()).isFalse()
        }

        @DisplayName("데드라인이 maxBoundary 이후인 경우 false를 반환한다")
        @Test
        fun `데드라인이 maxBoundary 이후인 경우 false를 반환한다`() {
            //given
            val maxBoundary = boundaryResolver.resolveMaxBoundary()
            val deadLine = maxBoundary.plusMinutes(1)
            val preferenceQuestRequest = PreferenceQuestRequest(title = "", deadLine = deadLine, boundaryResolver = boundaryResolver)

            //when

            //then
            assertThat(preferenceQuestRequest.isValidDeadLine()).isFalse()
        }

        @DisplayName("데드라인이 maxBoundary와 동일한 경우 false를 반환한다")
        @Test
        fun `데드라인이 maxBoundary와 동일한 경우 false를 반환한다`() {
            //given
            val maxBoundary = boundaryResolver.resolveMaxBoundary()
            val deadLine = maxBoundary
            val preferenceQuestRequest = PreferenceQuestRequest(title = "", deadLine = deadLine, boundaryResolver = boundaryResolver)

            //when

            //then
            assertThat(preferenceQuestRequest.isValidDeadLine()).isFalse()
        }

        @DisplayName("데드라인이 maxBoundary 이전인 경우 true를 반환한다")
        @Test
        fun `데드라인이 maxBoundary 이전인 경우 true를 반환한다`() {
            //given
            val maxBoundary = boundaryResolver.resolveMaxBoundary()
            val deadLine = maxBoundary.minusMinutes(1)
            val preferenceQuestRequest = PreferenceQuestRequest(title = "", deadLine = deadLine, boundaryResolver = boundaryResolver)

            //when

            //then
            assertThat(preferenceQuestRequest.isValidDeadLine()).isTrue()
        }

        @DisplayName("데드라인이 null이면 true를 반환한다")
        @Test
        fun `데드라인이 null이면 true를 반환한다`() {
            ///given
            val deadLine = null
            val preferenceQuestRequest = PreferenceQuestRequest(title = "", deadLine = deadLine, boundaryResolver = boundaryResolver)

            //when

            //then
            assertThat(preferenceQuestRequest.isValidDeadLine()).isTrue()
        }
    }

    @DisplayName("엔티티 변환 시")
    @Nested
    inner class TestMapToEntity {

        @DisplayName("isValidDeadLine 결과가 false면 예외를 던진다")
        @Test
        fun `isValidDeadLine 결과가 false면 예외를 던진다`() {
            //given
            val minBoundary = boundaryResolver.resolveMinBoundary()
            val deadLine = minBoundary
            val preferenceQuestRequest = PreferenceQuestRequest(title = "", deadLine = deadLine, boundaryResolver = boundaryResolver)

            //when

            //then
            assertThrows<IllegalArgumentException> { preferenceQuestRequest.mapToEntity(mock()) }
        }

        @DisplayName("isValidDeadLine 결과가 true면 예외를 던지지 않는다")
        @Test
        fun `isValidDeadLine 결과가 true면 예외를 던지지 않는다`() {
            //given
            val minBoundary = boundaryResolver.resolveMinBoundary()
            val deadLine = minBoundary.plusMinutes(10)
            val preferenceQuestRequest = PreferenceQuestRequest(title = "", deadLine = deadLine, boundaryResolver = boundaryResolver)

            //when

            //then
            assertDoesNotThrow { preferenceQuestRequest.mapToEntity(mock()) }
        }
    }

}